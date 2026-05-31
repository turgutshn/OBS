package com.turggut.sms.unit;

import com.turggut.sms.enrollment.service.GradeCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradeCalculatorComprehensiveTest {

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    @Test
    void weightedTotalHandlesPartialGrades() {
        assertEquals(bd("32.00"), GradeCalculator.weightedTotal(bd("80"), null));
        assertEquals(bd("54.00"), GradeCalculator.weightedTotal(null, bd("90")));
        assertNull(GradeCalculator.weightedTotal(null, null));
    }

    @Test
    void letterGradeCoversEveryBand() {
        assertNull(GradeCalculator.letterGrade(null));
        assertEquals("AA", GradeCalculator.letterGrade(bd("90")));
        assertEquals("BA", GradeCalculator.letterGrade(bd("85")));
        assertEquals("BB", GradeCalculator.letterGrade(bd("80")));
        assertEquals("CB", GradeCalculator.letterGrade(bd("75")));
        assertEquals("CC", GradeCalculator.letterGrade(bd("65")));
        assertEquals("DC", GradeCalculator.letterGrade(bd("58")));
        assertEquals("DD", GradeCalculator.letterGrade(bd("50")));
        assertEquals("FD", GradeCalculator.letterGrade(bd("40")));
        assertEquals("FF", GradeCalculator.letterGrade(bd("39")));
    }

    @Test
    void gradePointCoversEveryLetter() {
        assertNull(GradeCalculator.gradePoint(null));
        assertNull(GradeCalculator.gradePoint("ZZ"));
        assertEquals(bd("4.00"), GradeCalculator.gradePoint("AA"));
        assertEquals(bd("3.50"), GradeCalculator.gradePoint("BA"));
        assertEquals(bd("3.00"), GradeCalculator.gradePoint("BB"));
        assertEquals(bd("2.50"), GradeCalculator.gradePoint("CB"));
        assertEquals(bd("2.00"), GradeCalculator.gradePoint("CC"));
        assertEquals(bd("1.50"), GradeCalculator.gradePoint("DC"));
        assertEquals(bd("1.00"), GradeCalculator.gradePoint("DD"));
        assertEquals(bd("0.50"), GradeCalculator.gradePoint("FD"));
        assertEquals(BigDecimal.ZERO, GradeCalculator.gradePoint("FF"));
    }

    @Test
    void isPassingMatchesPolicy() {
        for (String passing : new String[]{"AA", "BA", "BB", "CB", "CC", "DC", "DD"}) {
            assertTrue(GradeCalculator.isPassing(passing), passing + " should pass");
        }
        assertFalse(GradeCalculator.isPassing("FD"));
        assertFalse(GradeCalculator.isPassing("FF"));
        assertFalse(GradeCalculator.isPassing(null));
        assertFalse(GradeCalculator.isPassing("??"));
    }
}
