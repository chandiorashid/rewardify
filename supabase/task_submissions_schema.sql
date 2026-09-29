-- =============================================================
-- Rewardify install-offer tasks — run ONCE (already deployed via the
-- Management API on 2026-09-29):
--   SQL Editor -> New query -> paste this file -> Run
-- =============================================================
-- What it creates:
--   * install_offers — admin-managed "install & try" tasks: app name,
--     Play Store package/url, reward (default $0.08), instructions,
--     is_active. The app lists active offers; you add/edit them in the
--     Table Editor (no app update needed).
--   * storage bucket "task-proofs" (private) — users upload screenshot
--     proof into their own folder <user-id>/. RLS keeps users to their
--     own folder; you review everything in the dashboard.
--   * task_submissions — one row per proof submission: the offer, the
--     screenshot paths, status pending | approved | rejected.
--   * trg_submission_decision — when you flip a submission from pending
--     to approved, the $0.08 reward (the offer's reward) is credited to
--     the user automatically, plus the 5% lifetime commission to their
--     referrer. Flipping to rejected just stamps decided_at.
--
-- Admin flow: Table Editor -> task_submissions -> open the screenshots
-- (Storage -> task-proofs -> <user-id>/) -> verify -> set status to
-- 'approved'. The app then shows Approved and the $0.08 lands in the
-- user's wallet by itself.
-- =============================================================

-- 1) Install offers (admin-managed) -----------------------------------
create table if not exists public.install_offers (
    id uuid primary key default gen_random_uuid(),
    app_name text not null,
    package_name text not null,
    store_url text not null,
    reward numeric(10, 2) not null default 0.08 check (reward > 0),
    instructions text not null default
        'Install the app from the Play Store, use it for 2 minutes, then submit screenshots as proof.',
    is_active boolean not null default true,
    created_at timestamptz not null default now()
);

create index if not exists install_offers_active_idx
    on public.install_offers (is_active) where is_active;

-- 2) Private storage bucket for screenshot proof -----------------------
insert into storage.buckets (id, name, public)
values ('task-proofs', 'task-proofs', false)
on conflict (id) do nothing;

drop policy if exists "proofs_insert_own" on storage.objects;
create policy "proofs_insert_own" on storage.objects
    for insert
    with check (
        bucket_id = 'task-proofs'
        and auth.uid()::text = (storage.foldername(name))[1]
    );

drop policy if exists "proofs_select_own" on storage.objects;
create policy "proofs_select_own" on storage.objects
    for select
    using (
        bucket_id = 'task-proofs'
        and auth.uid()::text = (storage.foldername(name))[1]
    );

-- 3) Submissions ---------------------------------------------------------
create table if not exists public.task_submissions (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.profiles (id) on delete cascade,
    offer_id uuid not null references public.install_offers (id) on delete cascade,
    screenshot_urls text[] not null default '{}',
    status text not null default 'pending'
        check (status in ('pending', 'approved', 'rejected')),
    admin_note text,
    submitted_at timestamptz not null default now(),
    decided_at timestamptz
);

create index if not exists task_submissions_user_idx
    on public.task_submissions (user_id);
create index if not exists task_submissions_status_idx
    on public.task_submissions (status);

-- One pending submission per user per offer (resubmit allowed after a
-- decision).
create unique index if not exists task_submissions_one_pending
    on public.task_submissions (user_id, offer_id)
    where status = 'pending';

-- 4) Row Level Security ----------------------------------------------------
alter table public.install_offers enable row level security;
alter table public.task_submissions enable row level security;

-- Everyone (including logged-out users) can see active offers.
drop policy if exists "install_offers_select_active" on public.install_offers;
create policy "install_offers_select_active" on public.install_offers
    for select using (is_active = true);

-- Users read and file their own submissions.
drop policy if exists "task_submissions_select_own" on public.task_submissions;
create policy "task_submissions_select_own" on public.task_submissions
    for select using (auth.uid() = user_id);

drop policy if exists "task_submissions_insert_own" on public.task_submissions;
create policy "task_submissions_insert_own" on public.task_submissions
    for insert with check (auth.uid() = user_id);

-- 5) Auto-credit on approval -------------------------------------------------
-- Fires only on the pending -> approved transition, so editing the row
-- again (or approving twice) can never double-pay.
create or replace function public.credit_approved_submission()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_reward numeric(10, 2);
    v_app_name text;
    v_referrer uuid;
    v_commission numeric(10, 2);
begin
    if NEW.status = 'approved' and OLD.status = 'pending' then
        select o.reward, o.app_name into v_reward, v_app_name
        from public.install_offers o
        where o.id = NEW.offer_id;
        v_reward := coalesce(v_reward, 0.08);

        insert into public.earnings (user_id, amount, source, note)
        values (NEW.user_id, v_reward, 'task',
                'Install offer approved: ' || coalesce(v_app_name, 'app'));

        select p.referred_by into v_referrer
        from public.profiles p
        where p.id = NEW.user_id;
        if v_referrer is not null then
            v_commission := round(v_reward * 0.05, 2);
            if v_commission > 0 then
                insert into public.earnings
                    (user_id, amount, source, related_user_id, note)
                values (v_referrer, v_commission, 'referral_commission',
                        NEW.user_id,
                        '5% lifetime commission from referral earnings');
            end if;
        end if;
        NEW.decided_at := now();
    elsif NEW.status = 'rejected' and OLD.status = 'pending' then
        NEW.decided_at := now();
    end if;
    return NEW;
end;
$$;

drop trigger if exists trg_submission_decision on public.task_submissions;
create trigger trg_submission_decision
before update on public.task_submissions
for each row
execute function public.credit_approved_submission();
