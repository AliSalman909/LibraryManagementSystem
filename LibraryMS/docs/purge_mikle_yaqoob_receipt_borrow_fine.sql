-- Purge borrow + fine history for the two sample receipts (Mikle / Spoken English 898WH,
-- Yaqoob / Computer Networks V2 CWQ25). Run manually against library_db when those rows exist.
-- Safe to re-run: deletes only matching rows.

USE library_db;

START TRANSACTION;

-- 1) Reservations on those student+title pairs (waitlist noise)
DELETE FROM reservations
WHERE (student_id = 'XEKLS9' AND book_id = '898WH')
   OR (student_id = '4QRTDX' AND book_id = 'CWQ25');

-- 2) Put affected copies back on shelf before we drop borrow_records (if any loan still held them)
UPDATE book_copies c
JOIN borrow_records br ON br.copy_id = c.copy_id
SET c.is_available = TRUE
WHERE (br.student_id = 'XEKLS9' AND br.book_id = '898WH')
   OR (br.student_id = '4QRTDX' AND br.book_id = 'CWQ25');

-- 3) Fines (FK -> borrow_records.record_id)
DELETE f FROM fines f
JOIN borrow_records br ON br.record_id = f.record_id
WHERE (br.student_id = 'XEKLS9' AND br.book_id = '898WH')
   OR (br.student_id = '4QRTDX' AND br.book_id = 'CWQ25');

-- 4) Borrow requests: deleting request cascades borrow_records (see fk_borrow_records_request ON DELETE CASCADE)
DELETE FROM borrow_requests
WHERE request_id IN (
    SELECT request_id FROM (
        SELECT br.request_id AS request_id
        FROM borrow_records br
        WHERE (br.student_id = 'XEKLS9' AND br.book_id = '898WH')
           OR (br.student_id = '4QRTDX' AND br.book_id = 'CWQ25')
    ) t
);

-- 5) Reconcile shelf counts for the two titles
UPDATE books b
SET b.available_copies = (
        SELECT COUNT(*)
        FROM book_copies c
        WHERE c.book_id = b.book_id AND c.is_available = TRUE
    ),
    b.updated_at = CURRENT_TIMESTAMP(6)
WHERE b.book_id IN ('898WH', 'CWQ25');

COMMIT;
