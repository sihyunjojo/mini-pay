-- 사용자 생성
INSERT INTO users (id, name, email, password, role, nickname)
VALUES (1, 'Admin', 'admin@example.com', 'securepassword', 'ADMIN', 'admin');

-- 계정 생성
INSERT INTO accounts (id, type, balance, user_id)
VALUES (1, 'MAIN', 1000000000, 1);