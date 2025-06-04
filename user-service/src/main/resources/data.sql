-- 插入测试用户数据 (密码都是 'password123' 的加密形式)
INSERT INTO users (username, password, email, nickname, level, experience, user_rank, is_enabled)
VALUES 
('admin', '$2a$10$rJU7TDl0hJDqxR3ZRLZzBeJ5UvxIHGJkCxhO.wU1qD7yNlAAfWX6q', 'admin@example.com', '管理员', 20, 20000, 'HISTORY_MASTER', true),
('test_user1', '$2a$10$rJU7TDl0hJDqxR3ZRLZzBeJ5UvxIHGJkCxhO.wU1qD7yNlAAfWX6q', 'user1@example.com', '历史爱好者1', 10, 10000, 'HISTORY_ENTHUSIAST', true),
('test_user2', '$2a$10$rJU7TDl0hJDqxR3ZRLZzBeJ5UvxIHGJkCxhO.wU1qD7yNlAAfWX6q', 'user2@example.com', '活跃用户2', 5, 5000, 'ACTIVE_VOTER', true),
('test_user3', '$2a$10$rJU7TDl0hJDqxR3ZRLZzBeJ5UvxIHGJkCxhO.wU1qD7yNlAAfWX6q', 'user3@example.com', '新人3', 1, 500, 'NEWCOMER', true),
('locked_user', '$2a$10$rJU7TDl0hJDqxR3ZRLZzBeJ5UvxIHGJkCxhO.wU1qD7yNlAAfWX6q', 'locked@example.com', '已锁定用户', 1, 0, 'NEWCOMER', true)
ON DUPLICATE KEY UPDATE id = id; 