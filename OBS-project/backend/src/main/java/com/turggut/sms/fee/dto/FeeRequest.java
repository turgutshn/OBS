package com.turggut.sms.fee.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeRequest(
        @NotNull Long studentId,
        @NotBlank @Size(max = 20) String semester,
        @NotNull @DecimalMin("0.0") BigDecimal amount,
        @NotNull @FutureOrPresent LocalDate dueDate,
        @Size(max = 255) String description) {}
