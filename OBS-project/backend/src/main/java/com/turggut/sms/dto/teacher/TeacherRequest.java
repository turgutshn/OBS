package com.turggut.sms.dto.teacher;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeacherRequest(
        @NotBlank @Size(max = 32) String employeeNumber,
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Email @NotBlank @Size(max = 160) String email,
        @Size(min = 8, max = 128) String password,
        @Size(max = 40) String title,
        @Size(max = 120) String department,
        @Size(max = 32) String phone) {}
