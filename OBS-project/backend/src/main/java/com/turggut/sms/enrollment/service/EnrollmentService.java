package com.turggut.sms.enrollment.service;

import com.turggut.sms.reporting.service.AuditLogService;

import com.turggut.sms.course.domain.Course;
import com.turggut.sms.course.domain.CourseRepository;
import com.turggut.sms.enrollment.domain.Enrollment;
import com.turggut.sms.enrollment.domain.EnrollmentRepository;
import com.turggut.sms.enrollment.domain.EnrollmentStatus;
import com.turggut.sms.student.domain.Student;
import com.turggut.sms.student.domain.StudentRepository;
import com.turggut.sms.enrollment.dto.EnrollmentRequest;
import com.turggut.sms.enrollment.dto.EnrollmentResponse;
import com.turggut.sms.enrollment.dto.GradeRequest;
import com.turggut.sms.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public EnrollmentResponse enroll(EnrollmentRequest request) {
        if (enrollmentRepository.existsByStudentIdAndCourseIdAndSemester(
                request.studentId(), request.courseId(), request.semester())) {
            throw ApiException.conflict("already_enrolled",
                    "Student is already enrolled in this course for the semester");
        }
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> ApiException.notFound("Student not found"));
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> ApiException.notFound("Course not found"));
        if (!course.isActive()) {
            throw ApiException.badRequest("course_inactive", "Course is not active");
        }
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .semester(request.semester())
                .status(EnrollmentStatus.ENROLLED)
                .build();
        enrollment = enrollmentRepository.save(enrollment);
        auditLogService.success("ENROLLMENT_CREATE", "Enrollment", enrollment.getId().toString(),
                "studentId=" + student.getId() + " courseId=" + course.getId());
        return EnrollmentResponse.from(enrollment);
    }

    @Transactional
    public void drop(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Enrollment not found"));
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        auditLogService.success("ENROLLMENT_DROP", "Enrollment", id.toString(), null);
    }

    @Transactional
    public void delete(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw ApiException.notFound("Enrollment not found");
        }
        enrollmentRepository.deleteById(id);
        auditLogService.success("ENROLLMENT_DELETE", "Enrollment", id.toString(), null);
    }

    @Transactional
    public EnrollmentResponse setGrade(Long id, GradeRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Enrollment not found"));
        enrollment.setMidtermGrade(request.midtermGrade());
        enrollment.setFinalGrade(request.finalGrade());
        BigDecimal total = GradeCalculator.weightedTotal(request.midtermGrade(), request.finalGrade());
        String letter = GradeCalculator.letterGrade(total);
        enrollment.setLetterGrade(letter);
        if (request.finalGrade() != null) {
            enrollment.setStatus(GradeCalculator.isPassing(letter)
                    ? EnrollmentStatus.COMPLETED : EnrollmentStatus.FAILED);
        }
        auditLogService.success("ENROLLMENT_GRADE", "Enrollment", id.toString(),
                "midterm=" + request.midtermGrade() + " final=" + request.finalGrade() + " letter=" + letter);
        return EnrollmentResponse.from(enrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> listByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> listByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(EnrollmentResponse::from)
                .toList();
    }
}
