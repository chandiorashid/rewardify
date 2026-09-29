-- =============================================================
-- Rewardify withdrawals — run ONCE in the Supabase dashboard
-- (already deployed via the Management API on 2026-09-29):
--   SQL Editor -> New query -> paste this file -> Run
-- =============================================================
-- What it creates:
--   * record_withdrawal(amount, note) — SECURITY DEFINER function the
--     app calls when the user withdraws. It writes a NEGATIVE 'withdrawal'
--     row into the earnings ledger, so:
--       - every withdrawal is tracked in Supabase (Table Editor -> earnings)
--       - the wallet balance (sum of the ledger) drops accordingly
--       - the withdrawal shows up in the app's transaction history
--   * Guards: must be signed in, amount > 0, and balance must cover it.
--     No insert RLS policy is needed (SECURITY DEFINER bypasses RLS,
--     same pattern as the other ledger functions).
-- =============================================================

create or replace function public.record_withdrawal(
    p_amount numeric,
    p_note text
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_uid uuid := auth.uid();
    v_balance numeric(10, 2);
begin
    if v_uid is null then
        raise exception 'Not authenticated';
    end if;
    if p_amount is null or p_amount <= 0 then
        raise exception 'Amount must be positive';
    end if;

    select coalesce(sum(amount), 0) into v_balance
    from public.earnings
    where user_id = v_uid;

    if v_balance < p_amount then
        raise exception 'Insufficient balance';
    end if;

    insert into public.earnings (user_id, amount, source, note)
    values (v_uid, -p_amount, 'withdrawal', nullif(trim(p_note), ''));
end;
$$;
