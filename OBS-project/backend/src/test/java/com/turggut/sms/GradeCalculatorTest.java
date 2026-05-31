package com.turggut.sms;

import com.turggut.sms.service.GradeCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradeCalculatorTest {

    @Test
    void weightedTotalAppliesMidtermAndFinalWeights() {
        BigDecimal total = GradeCalculator.weightedTotal(new BigDecimal("80"), new BigDecimal("90"));
        assertEquals(new BigDecimal("86.00"), total);
    }

    @Test
    void weightedTotalNullWhenBothMissing() {
        assertNull(GradeCalculator.weightedTotal(null, null));
    }

    @Test
    void letterGradeMapping() {
        assertEquals("AA", GradeCalculator.letterGrade(new BigDecimal("95")));
        assertEquals("CC", GradeCalculator.letterGrade(new BigDecimal("66")));
        assertEquals("FF", GradeCalculator.letterGrade(new BigDecimal("20")));
    }

    @Test
    void passingStatus() {
        assertTrue(GradeCalculator.isPassing("CC"));
        assertFalse(GradeCalculator.isPassing("FF"));
        assertFalse(GradeCalculator.isPassing(null));
    }
}
