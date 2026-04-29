package com.library.service;

import jakarta.annotation.PostConstruct;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReservationSchemaMigrationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationSchemaMigrationService.class);
    private static final String UNIQUE_INDEX_NAME = "uq_reservation_student_book_active";
    private static final String NORMAL_INDEX_NAME = "idx_reservation_student_book_status";
    private static final String STUDENT_INDEX_NAME = "idx_reservation_student_id";
    private static final String BOOK_INDEX_NAME = "idx_reservation_book_id";

    private final JdbcTemplate jdbcTemplate;

    public ReservationSchemaMigrationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureReservationHistoryFriendlyIndexes() {
        ensureIndex(STUDENT_INDEX_NAME, "create index " + STUDENT_INDEX_NAME + " on reservations (student_id)");
        ensureIndex(BOOK_INDEX_NAME, "create index " + BOOK_INDEX_NAME + " on reservations (book_id)");

        Integer uniqueCount = jdbcTemplate.query(
                """
                select count(*)
                from information_schema.statistics
                where table_schema = database()
                  and table_name = 'reservations'
                  and index_name = ?
                  and non_unique = 0
                """,
                ps -> ps.setString(1, UNIQUE_INDEX_NAME),
                rs -> rs.next() ? rs.getInt(1) : 0);

        if (uniqueCount != null && uniqueCount > 0) {
            try {
                log.info("Dropping unique reservation index {} to preserve status history.", UNIQUE_INDEX_NAME);
                jdbcTemplate.execute("alter table reservations drop index " + UNIQUE_INDEX_NAME);
            } catch (DataAccessException ex) {
                Throwable rootCause = ex.getMostSpecificCause();
                String causeMessage = rootCause != null ? rootCause.getMessage() : ex.getMessage();
                log.warn(
                        "Could not drop reservation unique index {} automatically ({}). "
                                + "App will continue; run manual DB migration if status-history conflicts persist.",
                        UNIQUE_INDEX_NAME,
                        causeMessage);
            }
        }

        ensureIndex(
                NORMAL_INDEX_NAME,
                "create index " + NORMAL_INDEX_NAME + " on reservations (student_id, book_id, status)");
    }

    private void ensureIndex(String indexName, String createSql) {
        Integer count = jdbcTemplate.query(
                """
                select count(*)
                from information_schema.statistics
                where table_schema = database()
                  and table_name = 'reservations'
                  and index_name = ?
                """,
                ps -> ps.setString(1, indexName),
                rs -> rs.next() ? rs.getInt(1) : 0);

        if (count == null || count == 0) {
            jdbcTemplate.execute(Objects.requireNonNull(createSql));
        }
    }
}
