package com.turggut.sms.dto.course;

import com.turggut.sms.domain.course.Course;

public record CourseResponse(
        Long id,
        String code,
        String name,
        String description,
        Integer credits,
        String department,
        String semester,
        Long teacherId,
        String teacherName,
        boolean active) {

    public static CourseResponse from(Course c) {
        return new CourseResponse(
                c.getId(),
                c.getCode(),
                c.getName(),
                c.getDescription(),
                c.getCredits(),
                c.getDepartment(),
                c.getSemester(),
                c.getTeacher() == null ? null : c.getTeacher().getId(),
                c.getTeacher() == null ? null : c.getTeacher().getFullName(),
                c.isActive());
    }
}
