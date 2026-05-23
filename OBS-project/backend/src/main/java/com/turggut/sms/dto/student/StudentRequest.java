package com.turggut.sms.dto.student;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record StudentRequest(
        @NotBlank @Size(max = 32) String studentNumber,
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Email @NotBlank @Size(max = 160) String email,
        @Size(min = 8, max = 128) String password,
        @Size(max = 32) String nationalId,
        @Size(max = 32) String phone,
        @Past LocalDate dateOfBirth,
        @Size(max = 120) String department,
        @Min(1900) @Max(2100) Integer enrollmentYear,
        @Size(max = 255) String address) {}
