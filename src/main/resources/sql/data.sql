-- 사용자 생성
INSERT INTO users (id, name, email, password, role, nickname)
VALUES (1, 'Admin', 'admin@example.com', 'securepassword', 'ADMIN', 'admin');

-- 계정 생성
INSERT INTO accounts (type, balance, user_id, daily_limit_used, limit_used, charge_limit)
VALUES ('MAIN', 1000000000, 1, 0, 0, 1000000000);
