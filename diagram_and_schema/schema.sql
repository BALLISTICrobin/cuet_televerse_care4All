-- ===========================================================================
-- CareForAll Donation Platform SQL Schema (Revised)
-- Addresses: Idempotency, Reliable Events (Outbox), State Control, Read Models
-- ===========================================================================

-- ===========================
-- USERS TABLE (Auth Service DB)
-- ===========================
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'donor',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================
-- CAMPAIGNS TABLE (Campaign Service DB)
-- ===========================
CREATE TABLE campaigns (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    target_amount NUMERIC(12,2),
    status VARCHAR(50) DEFAULT 'active',
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================
-- PLEDGES TABLE (Donation Service DB - Write Heavy)
-- ===========================
CREATE TABLE pledges (
    id UUID PRIMARY KEY,
    campaign_id UUID REFERENCES campaigns(id),
    user_id UUID REFERENCES users(id),
    donor_email VARCHAR(255),
    amount NUMERIC(12,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'initiated', -- initiated | authorized | captured | failed | refunded
    
    -- Idempotency Key for initial request protection
    idempotency_key VARCHAR(255) UNIQUE NOT NULL, 

    -- Version for optimistic locking on status updates
    version INT DEFAULT 1 NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================
-- PAYMENTS TABLE (Payment Service DB - Write Heavy)
-- ===========================
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    pledge_id UUID UNIQUE REFERENCES pledges(id), 
    
    -- ID provided by the external payment processor
    provider_transaction_id VARCHAR(255) NOT NULL, 
    
    -- Unique key from payment provider's webhook event to prevent duplicate processing
    webhook_idempotency_key VARCHAR(255) UNIQUE, 
    
    status VARCHAR(50) DEFAULT 'authorized', -- authorized | captured | refunded | failed
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================
-- OUTBOX EVENTS TABLE (Donation Service/Payment Service DB - Write Heavy)
-- ===========================
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================
-- CAMPAIGN TOTALS TABLE (Campaign Service DB - Read Heavy)
-- ===========================
CREATE TABLE campaign_totals (
    campaign_id UUID PRIMARY KEY REFERENCES campaigns(id),
    total_pledged NUMERIC(12,2) DEFAULT 0,
    total_captured NUMERIC(12,2) DEFAULT 0,
    donation_count INT DEFAULT 0, 
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP