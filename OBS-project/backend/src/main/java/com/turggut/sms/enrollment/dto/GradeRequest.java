package com.turggut.sms.enrollment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record GradeRequest(
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal midtermGrade,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal finalGrade) {}
