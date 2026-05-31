package com.turggut.sms.course.dto;

import jakarta.validation.constraints.*;

public record CourseRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 160) String name,
        @Size(max = 2000) String description,
        @NotNull @Min(1) @Max(12) Integer credits,
        @Size(max = 120) String department,
        @Size(max = 20) String semester,
        Long teacherId,
        Boolean active) {}
