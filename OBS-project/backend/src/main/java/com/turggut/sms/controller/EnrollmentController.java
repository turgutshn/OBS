package com.turggut.sms.controller;

import com.turggut.sms.domain.student.StudentRepository;
import com.turggut.sms.dto.enrollment.EnrollmentRequest;
import com.turggut.sms.dto.enrollment.EnrollmentResponse;
import com.turggut.sms.dto.enrollment.GradeRequest;
import com.turggut.sms.exception.ApiException;
import com.turggut.sms.security.AuthenticatedUser;
import com.turggut.sms.security.SecurityUtils;
import com.turggut.sms.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final StudentRepository studentRepository;

    @PostMapping("/admin/enrollments")
    @PreAuthorize("hasRole('ADMIN')")
    public EnrollmentResponse enroll(@Valid @RequestBody EnrollmentRequest request) {
        return enrollmentService.enroll(request);
    }

    @DeleteMapping("/admin/enrollments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/enrollments/{id}/drop")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> drop(@PathVariable Long id) {
        enrollmentService.drop(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/teacher/enrollments/{id}/grade")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public EnrollmentResponse setGrade(@PathVariable Long id, @Valid @RequestBody GradeRequest request) {
        return enrollmentService.setGrade(id, request);
    }

    @GetMapping("/admin/students/{studentId}/enrollments")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EnrollmentResponse> listByStudent(@PathVariable Long studentId) {
        return enrollmentService.listByStudent(studentId);
    }

    @GetMapping("/admin/courses/{courseId}/enrollments")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<EnrollmentResponse> listByCourse(@PathVariable Long courseId) {
        return enrollmentService.listByCourse(courseId);
    }

    @GetMapping("/student/enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    public List<EnrollmentResponse> myEnrollments() {
        AuthenticatedUser user = SecurityUtils.currentUser()
                .orElseThrow(() -> ApiException.unauthorized("Not authenticated"));
        Long studentId = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> ApiException.notFound("Student profile not found")).getId();
        return enrollmentService.listByStudent(studentId);
    }
}
