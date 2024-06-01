MERGE INTO users (user_id, user_name, user_login, user_email, user_birthday)
VALUES (1, 'testname1', 'testlogin1', 'email1@email.ru', CURRENT_TIMESTAMP());

MERGE INTO users (user_id, user_name, user_login, user_email, user_birthday)
VALUES (2, 'testname2', 'testlogin2', 'email2@email.ru', CURRENT_TIMESTAMP());