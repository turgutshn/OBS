package com.turggut.sms.dto.teacher;

import com.turggut.sms.domain.teacher.Teacher;

public record TeacherResponse(
        Long id,
        String employeeNumber,
        String firstName,
        String lastName,
        String email,
        String username,
        String title,
        String department,
        String phone,
        boolean active) {

    public static TeacherResponse from(Teacher t) {
        return new TeacherResponse(
                t.getId(),
                t.getEmployeeNumber(),
                t.getFirstName(),
                t.getLastName(),
                t.getUser().getEmail(),
                t.getUser().getUsername(),
                t.getTitle(),
                t.getDepartment(),
                t.getPhone(),
                t.getUser().isEnabled());
    }
}
