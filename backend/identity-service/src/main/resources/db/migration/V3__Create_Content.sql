CREATE TABLE content (
    event_id UUID PRIMARY KEY,

    source VARCHAR(50) NOT NULL,
    external_id VARCHAR(255) NOT NULL,

    author_event_id UUID NOT NULL,
    author_external_id VARCHAR(255) NOT NULL,

    text_content TEXT,
    language VARCHAR(10),
    created_at TIMESTAMPTZ,

    attachments JSONB DEFAULT '[]'::jsonb,
    metrics JSONB DEFAULT '{}'::jsonb,

    raw_data_reference TEXT NOT NULL,
    trace_id UUID NOT NULL,
    observed_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_content_source_external UNIQUE (source, external_id)
);

CREATE INDEX idx_content_author ON content (author_event_id);
CREATE INDEX idx_content_text ON content USING GIN (to_tsvector('english', text_content));
CREATE INDEX idx_content_metrics ON content USING GIN (metrics);