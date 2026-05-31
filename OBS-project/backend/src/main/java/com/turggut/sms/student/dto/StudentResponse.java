package com.turggut.sms.student.dto;

import com.turggut.sms.student.domain.Student;

import java.time.LocalDate;

public record StudentResponse(
        Long id,
        String studentNumber,
        String firstName,
        String lastName,
        String email,
        String username,
        String nationalId,
        String phone,
        LocalDate dateOfBirth,
        String department,
        Integer enrollmentYear,
        String address,
        boolean active) {

    public static StudentResponse from(Student s) {
        return new StudentResponse(
                s.getId(),
                s.getStudentNumber(),
                s.getFirstName(),
                s.getLastName(),
                s.getUser().getEmail(),
                s.getUser().getUsername(),
                s.getNationalId(),
                s.getPhone(),
                s.getDateOfBirth(),
                s.getDepartment(),
                s.getEnrollmentYear(),
                s.getAddress(),
                s.getUser().isEnabled());
    }
}
