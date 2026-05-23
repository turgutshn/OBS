package com.turggut.sms.dto.report;

import java.math.BigDecimal;
import java.util.Map;

public record SummaryReport(
        long totalStudents,
        long totalTeachers,
        long totalCourses,
        long totalEnrollments,
        BigDecimal totalAssessedFees,
        BigDecimal totalCollectedFees,
        BigDecimal totalOutstandingFees,
        Map<String, Long> studentsByDepartment,
        Map<String, Long> coursesByDepartment,
        Map<String, Long> feesByStatus) {}
