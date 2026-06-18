-- Default dev user: login test@test.com, password test
INSERT INTO users (email, password, first_name, last_name, role, enabled)
SELECT
    'test@test.com',
    '$2a$10$mnPKvw2diObG3CRDtGI27O3RWV.sDnih2d1G0KuOJT0Rw7a3teRmG',
    'Test',
    'User',
    'USER',
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'test@test.com');
