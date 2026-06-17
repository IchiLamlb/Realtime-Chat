INSERT INTO users (id, username, email, password_hash, display_name, avatar_url, bio, status)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'app_bot',
    'app-bot@realtime-chat.local',
    '$2a$10$disableddisableddisableddisab.ee5Y6bU/1rTyWJXwS7JjE2mYkzOT8u',
    'Realtime Assistant',
    NULL,
    'System bot for app help, troubleshooting, and chat workflow guidance.',
    'ACTIVE'
)
ON CONFLICT (id) DO NOTHING;
