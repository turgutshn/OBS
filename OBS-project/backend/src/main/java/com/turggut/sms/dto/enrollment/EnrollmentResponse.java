package com.turggut.sms.dto.enrollment;

import com.turggut.sms.domain.enrollment.Enrollment;

import java.math.BigDecimal;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        String studentNumber,
        String studentName,
        Long courseId,
        String courseCode,
        String courseName,
        Integer courseCredits,
        String teacherName,
        String semester,
        BigDecimal midtermGrade,
        BigDecimal finalGrade,
        String letterGrade,
        BigDecimal gradePoint,
        String status) {

    public static EnrollmentResponse from(Enrollment e) {
        return new EnrollmentResponse(
                e.getId(),
                e.getStudent().getId(),
                e.getStudent().getStudentNumber(),
                e.getStudent().getFullName(),
                e.getCourse().getId(),
                e.getCourse().getCode(),
                e.getCourse().getName(),
                e.getCourse().getCredits(),
                e.getCourse().getTeacher() == null ? null : e.getCourse().getTeacher().getFullName(),
                e.getSemester(),
                e.getMidtermGrade(),
                e.getFinalGrade(),
                e.getLetterGrade(),
                com.turggut.sms.service.GradeCalculator.gradePoint(e.getLetterGrade()),
                e.getStatus().name());
    }
}
