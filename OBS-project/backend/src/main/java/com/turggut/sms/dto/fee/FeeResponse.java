package com.turggut.sms.dto.fee;

import com.turggut.sms.domain.fee.Fee;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record FeeResponse(
        Long id,
        Long studentId,
        String studentNumber,
        String studentName,
        String semester,
        BigDecimal amount,
        BigDecimal paidAmount,
        BigDecimal outstanding,
        LocalDate dueDate,
        Instant paidAt,
        String status,
        String description) {

    public static FeeResponse from(Fee f) {
        return new FeeResponse(
                f.getId(),
                f.getStudent().getId(),
                f.getStudent().getStudentNumber(),
                f.getStudent().getFullName(),
                f.getSemester(),
                f.getAmount(),
                f.getPaidAmount(),
                f.getOutstanding(),
                f.getDueDate(),
                f.getPaidAt(),
                f.getStatus().name(),
                f.getDescription());
    }
}
