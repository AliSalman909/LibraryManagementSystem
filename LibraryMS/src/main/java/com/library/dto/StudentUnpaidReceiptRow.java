package com.library.dto;

import com.library.entity.Fine;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/** One combined unpaid-fine receipt row per student (multiple fine lines). */
public record StudentUnpaidReceiptRow(
        String studentUserId,
        String fullName,
        String email,
        int fineCount,
        BigDecimal totalAmount,
        BigDecimal totalWaived,
        BigDecimal totalNet,
        long receiptIdNumeric,
        List<Fine> fines) {

    public static long combinedReceiptId(String studentUserId) {
        long hash = Integer.toUnsignedLong(Objects.requireNonNull(studentUserId).hashCode());
        return 200000000L + (hash % 800000000L);
    }
}
