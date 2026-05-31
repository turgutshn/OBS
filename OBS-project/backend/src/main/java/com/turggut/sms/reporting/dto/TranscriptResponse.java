package com.turggut.sms.reporting.dto;

import com.turggut.sms.enrollment.dto.EnrollmentResponse;

import java.math.BigDecimal;
import java.util.List;

public record TranscriptResponse(
        Long studentId,
        String studentNumber,
        String studentName,
        String department,
        Integer enrollmentYear,
        List<SemesterBlock> semesters,
        BigDecimal cumulativeGpa,
        Integer totalCredits,
        Integer completedCredits) {

    public record SemesterBlock(
            String semester,
            List<EnrollmentResponse> enrollments,
            BigDecimal semesterGpa,
            Integer semesterCredits) {}
}
