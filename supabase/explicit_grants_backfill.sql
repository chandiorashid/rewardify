-- =============================================================
-- Rewardify explicit Data API grants — BACKFILL (safe, idempotent)
-- Prepared 2026-09-30. Not yet deployed.
-- Why: from 2026-10-30 Supabase stops auto-granting Data API access
-- (anon / authenticated / service_role) on new `public` tables.
-- Existing tables keep their current grants, so nothing breaks today,
-- but every table here was deployed with ZERO explicit GRANTs —
-- a rebuild from these migration files, or any future table added
-- after Oct 30, would hit PostgREST 42501 "permission denied".
-- This file pins the exact access each role needs (least privilege,
-- matching the RLS policies and the app's actual Data API usage),
-- so a fresh build behaves exactly like production.
-- Run once in SQL Editor, or via the Management API /database/query.
-- Convention going forward: every new `public` table ships its
-- GRANTs in the same migration file that creates it.
-- =============================================================

-- Tables ---------------------------------------------------------

-- profiles: the app reads the signed-in user's own profile and the
-- profiles of users they referred. All writes go through the
-- SECURITY DEFINER register_profile() function (no insert/update
-- policies exist), so SELECT is all the API role needs.
grant select on table public.profiles to authenticated;

-- earnings: the app reads its own ledger rows. Every write
-- (task earnings, commissions, withdrawals, approvals) goes through
-- SECURITY DEFINER functions / trigger, so SELECT is enough.
grant select on table public.earnings to authenticated;

-- payout_methods: the app lists, adds, edits (set default) and
-- deletes the user's own payout methods (RLS policy is FOR ALL on
-- own rows).
grant select, insert, update, delete
    on table public.payout_methods to authenticated;

-- withdrawals: the app reads its own withdrawal history. Rows are
-- created by the SECURITY DEFINER request_withdrawal() function;
-- status flips are done by the admin in the dashboard (service_role
-- bypasses RLS).
grant select on table public.withdrawals to authenticated;

-- install_offers: RLS policy intentionally allows everyone —
-- including logged-out users — to read active offers, so anon
-- keeps SELECT too.
grant select on table public.install_offers to anon, authenticated;

-- task_submissions: the app reads and files its own submissions.
-- Approval/rejection side-effects run in the SECURITY DEFINER
-- trigger; admin review is via the dashboard (service_role).
grant select, insert on table public.task_submissions to authenticated;

-- Service role -------------------------------------------------------
-- The admin dashboard (Table Editor) operates as service_role, which
-- bypasses RLS but still needs table-level GRANTs. Pin full access so
-- a rebuild never locks the admin out of their own tables.
grant all on table public.profiles to service_role;
grant all on table public.earnings to service_role;
grant all on table public.payout_methods to service_role;
grant all on table public.withdrawals to service_role;
grant all on table public.install_offers to service_role;
grant all on table public.task_submissions to service_role;

-- RPC functions ----------------------------------------------------
-- Explicit EXECUTE so future default-privilege changes cannot
-- silently break the app's RPC calls. resolve_referrer is designed
-- safe to call before signup (anonymous), hence anon keeps it.

grant execute on function public.resolve_referrer(text) to anon, authenticated;
grant execute on function public.register_profile(text, uuid) to authenticated;
grant execute on function public.record_task_earning(numeric, text) to authenticated;
grant execute on function public.record_withdrawal(numeric, text) to authenticated;
grant execute on function public.request_withdrawal(uuid, numeric) to authenticated;
grant execute on function public.set_default_payout_method(uuid) to authenticated;

-- Verify (run separately) -------------------------------------------
-- select table_name, grantee,
--        string_agg(privilege_type, ',' order by privilege_type) as privs
-- from information_schema.role_table_grants
-- where table_schema = 'public'
--   and grantee in ('anon','authenticated','service_role')
-- group by table_name, grantee
-- order by table_name, grantee;
