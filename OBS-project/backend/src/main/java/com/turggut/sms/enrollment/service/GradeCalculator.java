package com.turggut.sms.enrollment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GradeCalculator {

    private GradeCalculator() {}

    public static BigDecimal weightedTotal(BigDecimal midterm, BigDecimal finalGrade) {
        if (midterm == null && finalGrade == null) return null;
        BigDecimal mid = midterm == null ? BigDecimal.ZERO : midterm;
        BigDecimal fin = finalGrade == null ? BigDecimal.ZERO : finalGrade;
        // 40% midterm + 60% final
        return mid.multiply(new BigDecimal("0.40"))
                .add(fin.multiply(new BigDecimal("0.60")))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static String letterGrade(BigDecimal total) {
        if (total == null) return null;
        double t = total.doubleValue();
        if (t >= 90) return "AA";
        if (t >= 85) return "BA";
        if (t >= 80) return "BB";
        if (t >= 75) return "CB";
        if (t >= 65) return "CC";
        if (t >= 58) return "DC";
        if (t >= 50) return "DD";
        if (t >= 40) return "FD";
        return "FF";
    }

    public static BigDecimal gradePoint(String letter) {
        if (letter == null) return null;
        return switch (letter) {
            case "AA" -> new BigDecimal("4.00");
            case "BA" -> new BigDecimal("3.50");
            case "BB" -> new BigDecimal("3.00");
            case "CB" -> new BigDecimal("2.50");
            case "CC" -> new BigDecimal("2.00");
            case "DC" -> new BigDecimal("1.50");
            case "DD" -> new BigDecimal("1.00");
            case "FD" -> new BigDecimal("0.50");
            case "FF" -> BigDecimal.ZERO;
            default -> null;
        };
    }

    public static boolean isPassing(String letter) {
        if (letter == null) return false;
        return switch (letter) {
            case "AA", "BA", "BB", "CB", "CC", "DC", "DD" -> true;
            default -> false;
        };
    }
}
