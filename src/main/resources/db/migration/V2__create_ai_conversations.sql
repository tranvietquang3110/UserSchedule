-- Tạo bảng lưu nội dung hội thoại giữa người dùng và AI
CREATE TABLE ai_conversations (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    message_order INT NOT NULL,
    message NVARCHAR(MAX) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
