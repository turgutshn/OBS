package com.turggut.sms.enrollment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EnrollmentRequest(
        @NotNull Long studentId,
        @NotNull Long courseId,
        @NotBlank @Size(max = 20) String semester) {}
