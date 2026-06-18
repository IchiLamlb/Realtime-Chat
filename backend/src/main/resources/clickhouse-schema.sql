CREATE TABLE IF NOT EXISTS analytics_message_metrics (
    window_start DateTime,
    window_end DateTime,
    conversation_id UUID,
    message_count UInt64,
    unique_senders UInt64
) ENGINE = MergeTree
ORDER BY (window_start, conversation_id);

CREATE TABLE IF NOT EXISTS analytics_active_users (
    window_start DateTime,
    window_end DateTime,
    active_users UInt64
) ENGINE = MergeTree
ORDER BY window_start;

CREATE TABLE IF NOT EXISTS analytics_top_conversations (
    window_start DateTime,
    window_end DateTime,
    conversation_id UUID,
    message_count UInt64
) ENGINE = MergeTree
ORDER BY (window_start, message_count, conversation_id);

CREATE TABLE IF NOT EXISTS analytics_rate_limit_ratio (
    window_start DateTime,
    window_end DateTime,
    total_events UInt64,
    rate_limited_events UInt64,
    rate_limit_ratio Float64
) ENGINE = MergeTree
ORDER BY window_start;
