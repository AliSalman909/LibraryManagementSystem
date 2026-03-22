-- Run on library_db if you still have legacy U1:-prefixed encrypted user_ids or hit "Data too long" on user_id FKs.
-- Plain user IDs are 6 characters; VARCHAR(36) is enough for new data. Widen columns only if you keep long legacy values.

USE library_db;

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE users MODIFY COLUMN user_id VARCHAR(512) NOT NULL;

ALTER TABLE students MODIFY COLUMN user_id VARCHAR(512) NOT NULL;
ALTER TABLE librarians MODIFY COLUMN user_id VARCHAR(512) NOT NULL;
ALTER TABLE admins MODIFY COLUMN user_id VARCHAR(512) NOT NULL;

ALTER TABLE registration_requests MODIFY COLUMN user_id VARCHAR(512) NOT NULL;
ALTER TABLE deletion_requests MODIFY COLUMN user_id VARCHAR(512) NOT NULL;

ALTER TABLE notifications MODIFY COLUMN recipient_id VARCHAR(512) NOT NULL;
ALTER TABLE notifications MODIFY COLUMN sender_id VARCHAR(512) NULL;

SET FOREIGN_KEY_CHECKS = 1;
