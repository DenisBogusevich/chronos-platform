CREATE TABLE profiles (
    event_id UUID PRIMARY KEY,

    source VARCHAR(50) NOT NULL,
    external_id VARCHAR(255) NOT NULL,

    username VARCHAR(255),
    display_name VARCHAR(255),
    url TEXT,

    raw_data_reference TEXT NOT NULL,

    trace_id UUID NOT NULL,

    observed_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW(),

    location GEOGRAPHY(POINT, 4326),

    details JSONB DEFAULT '{}'::jsonb,

    CONSTRAINT uq_profiles_source_external UNIQUE (source, external_id)
);

CREATE INDEX idx_profiles_details ON profiles USING GIN (details);

CREATE INDEX idx_profiles_location ON profiles USING GIST (location);

CREATE INDEX idx_profiles_observed_at ON profiles (observed_at DESC);