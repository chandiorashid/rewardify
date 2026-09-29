-- =============================================================
-- Rewardify referral system — run ONCE in the Supabase dashboard:
--   SQL Editor -> New query -> paste this file -> Run
-- =============================================================
-- What it creates:
--   * profiles table  — one row per user: referral_code + referred_by link
--   * earnings table  — EVERY earning event is tracked here (the ledger
--                       the lifetime 5% referral commission is paid from)
--   * Row Level Security so users only ever see their own data
--   * Three secure functions the app calls:
--       resolve_referrer(code)        -> who owns a referral code
--       register_profile(name, referrer) -> creates the profile, generates a
--                                          unique code, credits the $0.20
--                                          welcome bonus when referred
--       record_task_earning(amount, note) -> logs a task earning AND pays
--                                          5% lifetime commission to the
--                                          referrer automatically
-- =============================================================

-- 1) Profiles ----------------------------------------------------
create table if not exists public.profiles (
    id uuid primary key references auth.users (id) on delete cascade,
    display_name text,
    referral_code text unique not null,
    referred_by uuid references public.profiles (id) on delete set null,
    created_at timestamptz not null default now()
);

-- 2) Earnings ledger ---------------------------------------------
-- source: 'task' | 'signup_bonus' | 'referral_commission'
--         | 'withdrawal' | 'adjustment'
create table if not exists public.earnings (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.profiles (id) on delete cascade,
    amount numeric(10, 2) not null,
    source text not null,
    related_user_id uuid references public.profiles (id) on delete set null,
    note text,
    created_at timestamptz not null default now()
);

create index if not exists earnings_user_idx on public.earnings (user_id);
create index if not exists earnings_source_idx on public.earnings (source);
create index if not exists profiles_referred_by_idx on public.profiles (referred_by);
create index if not exists profiles_code_idx on public.profiles (referral_code);

-- 3) Row Level Security -------------------------------------------
alter table public.profiles enable row level security;
alter table public.earnings enable row level security;

drop policy if exists "profiles_select_own" on public.profiles;
create policy "profiles_select_own" on public.profiles
    for select using (auth.uid() = id);

drop policy if exists "profiles_select_my_referrals" on public.profiles;
create policy "profiles_select_my_referrals" on public.profiles
    for select using (referred_by = auth.uid());

-- Writes go through the SECURITY DEFINER functions below, so no
-- insert/update policies are needed.

drop policy if exists "earnings_select_own" on public.earnings;
create policy "earnings_select_own" on public.earnings
    for select using (auth.uid() = user_id);

-- 4) Resolve a referral code -> referrer id ------------------------
-- Safe to call before signup (anonymous), reveals nothing but the id.
create or replace function public.resolve_referrer(p_code text)
returns table (id uuid)
language sql
security definer
set search_path = public
as $$
    select p.id
    from public.profiles p
    where upper(p.referral_code) = upper(trim(p_code))
    limit 1;
$$;

-- 5) Register the signed-in user's profile -----------------------
-- Idempotent: returns the existing code if the profile already exists.
-- Generates a unique 8-character code server-side and credits the
-- $0.20 welcome bonus when a valid referrer was supplied.
create or replace function public.register_profile(
    p_display_name text,
    p_referred_by uuid
)
returns table (referral_code text)
language plpgsql
security definer
set search_path = public
as $$
declare
    v_uid uuid := auth.uid();
    v_code text;
    v_existing_code text;
    v_referrer uuid;
    attempts int := 0;
    alphabet constant text := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
begin
    if v_uid is null then
        raise exception 'Not authenticated';
    end if;

    select p.referral_code into v_existing_code
    from public.profiles p where p.id = v_uid;
    if found then
        return query select v_existing_code;
        return;
    end if;

    -- Validate the referrer: must exist and cannot be yourself.
    if p_referred_by is not null and p_referred_by <> v_uid then
        select p.id into v_referrer
        from public.profiles p where p.id = p_referred_by;
    end if;

    -- Unique code, retried server-side on collision.
    loop
        attempts := attempts + 1;
        v_code := '';
        for i in 1..8 loop
            v_code := v_code || substr(alphabet, (floor(random() * length(alphabet)) + 1)::int, 1);
        end loop;
        begin
            insert into public.profiles (id, display_name, referral_code, referred_by)
            values (v_uid, nullif(trim(p_display_name), ''), v_code, v_referrer);
            exit;
        exception when unique_violation then
            if attempts >= 10 then raise; end if;
        end;
    end loop;

    -- $0.20 welcome bonus for joining through a referral.
    if v_referrer is not null then
        insert into public.earnings (user_id, amount, source, related_user_id, note)
        values (v_uid, 0.20, 'signup_bonus', v_referrer,
                'Welcome bonus for joining with a referral code');
    end if;

    return query select v_code;
end;
$$;

-- 6) Record a task earning + lifetime 5% referrer commission -------
-- One call logs the earner's credit AND, when the earner was referred,
-- pays round(amount * 5%) to the referrer forever. The commission is
-- only calculated on task earnings (not on bonuses).
create or replace function public.record_task_earning(
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
    v_referrer uuid;
    v_commission numeric(10, 2);
begin
    if v_uid is null then
        raise exception 'Not authenticated';
    end if;
    if p_amount is null or p_amount <= 0 then
        raise exception 'Amount must be positive';
    end if;

    insert into public.earnings (user_id, amount, source, note)
    values (v_uid, p_amount, 'task', nullif(trim(p_note), ''));

    select p.referred_by into v_referrer
    from public.profiles p where p.id = v_uid;

    if v_referrer is not null then
        v_commission := round(p_amount * 0.05, 2);
        if v_commission > 0 then
            insert into public.earnings (user_id, amount, source, related_user_id, note)
            values (v_referrer, v_commission, 'referral_commission', v_uid,
                    '5% lifetime commission from referral earnings');
        end if;
    end if;
end;
$$;
