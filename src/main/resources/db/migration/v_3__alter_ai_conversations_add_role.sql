ALTER TABLE ai_conversations
ADD role VARCHAR(10) NOT NULL DEFAULT 'user'
    CONSTRAINT chk_role CHECK (role IN ('user', 'ai'));
