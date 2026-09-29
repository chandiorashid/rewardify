-- =============================================================
-- Rewardify payouts — run ONCE (already deployed via the Management
-- API on 2026-09-29):
--   SQL Editor -> New query -> paste this file -> Run
-- =============================================================
-- What it creates:
--   * payout_methods — the user's saved payout destinations:
--       - 'usdc': wallet address + network (solana, ethereum, ...)
--       - 'exchange': binance / okx / bybit / ... + exchange UID
--     Users manage only their own rows (RLS).
--   * withdrawals — every withdrawal request:
--       - amount + a snapshot of the payout destination (so history stays
--         accurate even if the method is later deleted)
--       - status: 'pending' -> 'paid' | 'rejected'
--     The admin (you) flips pending -> paid in the Supabase dashboard
--     Table Editor after sending the money. The app then shows "Received".
--   * request_withdrawal(method_id, amount) — SECURITY DEFINER function
--     the app calls: validates the method, enforces the $5 minimum and
--     sufficient balance, locks the funds immediately with a negative
--     'withdrawal' ledger row, and creates the pending withdrawal.
--   * set_default_payout_method(id) — marks one method as default.
-- =============================================================

-- 1) Payout methods ------------------------------------------------
create table if not exists public.payout_methods (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.profiles (id) on delete cascade,
    method_type text not null check (method_type in ('usdc', 'exchange')),
    label text not null,
    exchange text,
    account_ref text not null,
    network text,
    is_default boolean not null default false,
    created_at timestamptz not null default now()
);

create index if not exists payout_methods_user_idx
    on public.payout_methods (user_id);

-- 2) Withdrawals ----------------------------------------------------
create table if not exists public.withdrawals (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.profiles (id) on delete cascade,
    payout_method_id uuid references public.payout_methods (id) on delete set null,
    amount numeric(10, 2) not null check (amount > 0),
    method_type text not null,
    method_label text not null,
    account_ref text not null,
    exchange text,
    network text,
    status text not null default 'pending'
        check (status in ('pending', 'paid', 'rejected')),
    admin_note text,
    requested_at timestamptz not null default now(),
    decided_at timestamptz
);

create index if not exists withdrawals_user_idx
    on public.withdrawals (user_id);
create index if not exists withdrawals_status_idx
    on public.withdrawals (status);

-- 3) Row Level Security ---------------------------------------------
alter table public.payout_methods enable row level security;
alter table public.withdrawals enable row level security;

drop policy if exists "payout_methods_all_own" on public.payout_methods;
create policy "payout_methods_all_own" on public.payout_methods
    for all using (auth.uid() = user_id)
    with check (auth.uid() = user_id);

-- Users read their own withdrawals. Inserts happen through the
-- request_withdrawal() function below; status changes are done by the
-- admin in the dashboard (service role bypasses RLS).
drop policy if exists "withdrawals_select_own" on public.withdrawals;
create policy "withdrawals_select_own" on public.withdrawals
    for select using (auth.uid() = user_id);

-- 4) Request a withdrawal --------------------------------------------
-- Locks the funds immediately (negative ledger row) and creates a
-- 'pending' withdrawal for the admin to pay out.
create or replace function public.request_withdrawal(
    p_payout_method_id uuid,
    p_amount numeric
)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
    v_uid uuid := auth.uid();
    v_method public.payout_methods%rowtype;
    v_balance numeric(10, 2);
    v_withdrawal_id uuid;
begin
    if v_uid is null then
        raise exception 'Not authenticated';
    end if;
    if p_amount is null or p_amount < 5 then
        raise exception 'Minimum withdrawal is $5.00';
    end if;

    select * into v_method
    from public.payout_methods
    where id = p_payout_method_id and user_id = v_uid;
    if not found then
        raise exception 'Payout method not found';
    end if;

    select coalesce(sum(amount), 0) into v_balance
    from public.earnings
    where user_id = v_uid;
    if v_balance < p_amount then
        raise exception 'Insufficient balance';
    end if;

    insert into public.earnings (user_id, amount, source, note)
    values (v_uid, -p_amount, 'withdrawal',
            'Withdrawal to ' || v_method.label);

    insert into public.withdrawals (
        user_id, payout_method_id, amount,
        method_type, method_label, account_ref, exchange, network
    )
    values (
        v_uid, v_method.id, p_amount,
        v_method.method_type, v_method.label,
        v_method.account_ref, v_method.exchange, v_method.network
    )
    returning id into v_withdrawal_id;

    return v_withdrawal_id;
end;
$$;

-- 5) Set the default payout method ------------------------------------
create or replace function public.set_default_payout_method(p_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_uid uuid := auth.uid();
begin
    if v_uid is null then
        raise exception 'Not authenticated';
    end if;
    if not exists (
        select 1 from public.payout_methods
        where id = p_id and user_id = v_uid
    ) then
        raise exception 'Payout method not found';
    end if;
    update public.payout_methods
    set is_default = false
    where user_id = v_uid;
    update public.payout_methods
    set is_default = true
    where id = p_id and user_id = v_uid;
end;
$$;
