-- ============================================================
-- NEXGEN BY KGC - INSFORGE (POSTGRESQL) DATABASE SCHEMA
-- Copy and paste this script directly into your Insforge SQL Editor
-- Project URL: https://ywy9d5pe.eu-central.insforge.app
-- ============================================================

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS public.users (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'STUDENT',
    avatar_url TEXT DEFAULT '',
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint
);

-- 2. COURSES TABLE
CREATE TABLE IF NOT EXISTS public.courses (
    id VARCHAR(255) PRIMARY KEY,
    tutor_id VARCHAR(255) NOT NULL,
    tutor_name VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    category VARCHAR(100) NOT NULL DEFAULT 'Coding for Kids',
    price_ngn NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    thumbnail_url TEXT NOT NULL DEFAULT '',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL',
    rejection_reason TEXT NOT NULL DEFAULT '',
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint
);

-- 3. LESSONS TABLE
CREATE TABLE IF NOT EXISTS public.lessons (
    id VARCHAR(255) PRIMARY KEY,
    course_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    youtube_url TEXT NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 15,
    order_index INT NOT NULL DEFAULT 1
);

-- 4. ENROLLMENTS TABLE
CREATE TABLE IF NOT EXISTS public.enrollments (
    id VARCHAR(255) PRIMARY KEY,
    student_id VARCHAR(255) NOT NULL,
    course_id VARCHAR(255) NOT NULL,
    enrolled_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint,
    progress_percentage INT NOT NULL DEFAULT 0,
    completed_lessons_json TEXT NOT NULL DEFAULT '',
    notes TEXT NOT NULL DEFAULT '',
    CONSTRAINT unique_student_course UNIQUE (student_id, course_id)
);

-- 5. TRANSACTIONS TABLE (Remita Audit Log)
CREATE TABLE IF NOT EXISTS public.transactions (
    id VARCHAR(255) PRIMARY KEY,
    student_id VARCHAR(255) NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    student_email VARCHAR(255) NOT NULL,
    course_id VARCHAR(255) NOT NULL,
    course_title VARCHAR(255) NOT NULL,
    amount_ngn NUMERIC(12, 2) NOT NULL,
    remita_service_fee_ngn NUMERIC(12, 2) NOT NULL DEFAULT 100.00,
    total_amount_ngn NUMERIC(12, 2) NOT NULL,
    remita_rrr VARCHAR(100) UNIQUE NOT NULL,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'REMITA_CARD',
    status VARCHAR(50) NOT NULL DEFAULT 'SUCCESSFUL',
    timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint
);

-- 6. INDEXES FOR FAST QUERY PERFORMANCE
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);
CREATE INDEX IF NOT EXISTS idx_courses_status ON public.courses(status);
CREATE INDEX IF NOT EXISTS idx_courses_tutor ON public.courses(tutor_id);
CREATE INDEX IF NOT EXISTS idx_lessons_course ON public.lessons(course_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_student ON public.enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_transactions_student ON public.transactions(student_id);

-- 7. GRANT ANONYMOUS & AUTHENTICATED ACCESS
GRANT ALL ON ALL TABLES IN SCHEMA public TO anon;
GRANT ALL ON ALL TABLES IN SCHEMA public TO authenticated;
