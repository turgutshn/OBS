package com.turggut.sms.unit;

import com.turggut.sms.course.domain.Course;
import com.turggut.sms.course.domain.CourseRepository;
import com.turggut.sms.enrollment.domain.Enrollment;
import com.turggut.sms.enrollment.domain.EnrollmentRepository;
import com.turggut.sms.enrollment.domain.EnrollmentStatus;
import com.turggut.sms.enrollment.dto.EnrollmentRequest;
import com.turggut.sms.enrollment.dto.EnrollmentResponse;
import com.turggut.sms.enrollment.dto.GradeRequest;
import com.turggut.sms.enrollment.service.EnrollmentService;
import com.turggut.sms.iam.domain.Role;
import com.turggut.sms.iam.domain.User;
import com.turggut.sms.reporting.service.AuditLogService;
import com.turggut.sms.shared.exception.ApiException;
import com.turggut.sms.student.domain.Student;
import com.turggut.sms.student.domain.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EnrollmentServiceUnitTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Student testStudent() {
        User user = User.builder()
                .id(1L)
                .username("stu001")
                .email("student@example.com")
                .role(Role.STUDENT)
                .enabled(true)
                .build();
        return Student.builder()
                .id(1L)
                .user(user)
                .studentNumber("STU001")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    private Course testCourse() {
        return Course.builder()
                .id(1L)
                .code("CS101")
                .name("Introduction to Computer Science")
                .credits(3)
                .active(true)
                .build();
    }

    private Enrollment testEnrollment() {
        return Enrollment.builder()
                .id(1L)
                .student(testStudent())
                .course(testCourse())
                .semester("Fall 2024")
                .status(EnrollmentStatus.ENROLLED)
                .build();
    }

    @Test
    void enrollStudentSuccessfully() {
        Student student = testStudent();
        Course course = testCourse();
        Enrollment enrollment = testEnrollment();

        EnrollmentRequest request = new EnrollmentRequest(1L, 1L, "Fall 2024");

        when(enrollmentRepository.existsByStudentIdAndCourseIdAndSemester(1L, 1L, "Fall 2024"))
                .thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        EnrollmentResponse response = enrollmentService.enroll(request);

        assertNotNull(response);
        assertEquals(EnrollmentStatus.ENROLLED.name(), response.status());
        verify(auditLogService).success(eq("ENROLLMENT_CREATE"), eq("Enrollment"), anyString(), anyString());
    }

    @Test
    void enrollStudentAlreadyEnrolled() {
        EnrollmentRequest request = new EnrollmentRequest(1L, 1L, "Fall 2024");

        when(enrollmentRepository.existsByStudentIdAndCourseIdAndSemester(1L, 1L, "Fall 2024"))
                .thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> enrollmentService.enroll(request));
        assertTrue(exception.getCode().contains("already_enrolled"));
    }

    @Test
    void enrollWithNonExistentStudent() {
        EnrollmentRequest request = new EnrollmentRequest(999L, 1L, "Fall 2024");

        when(enrollmentRepository.existsByStudentIdAndCourseIdAndSemester(999L, 1L, "Fall 2024"))
                .thenReturn(false);
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> enrollmentService.enroll(request));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void enrollWithNonExistentCourse() {
        Student student = testStudent();
        EnrollmentRequest request = new EnrollmentRequest(1L, 999L, "Fall 2024");

        when(enrollmentRepository.existsByStudentIdAndCourseIdAndSemester(1L, 999L, "Fall 2024"))
                .thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> enrollmentService.enroll(request));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void enrollInActiveCourse() {
        Student student = testStudent();
        Course course = testCourse();
        course.setActive(false);

        EnrollmentRequest request = new EnrollmentRequest(1L, 1L, "Fall 2024");

        when(enrollmentRepository.existsByStudentIdAndCourseIdAndSemester(1L, 1L, "Fall 2024"))
                .thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        ApiException exception = assertThrows(ApiException.class, () -> enrollmentService.enroll(request));
        assertTrue(exception.getCode().contains("inactive"));
    }

    @Test
    void dropEnrollmentSuccessfully() {
        Enrollment enrollment = testEnrollment();

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        enrollmentService.drop(1L);

        assertEquals(EnrollmentStatus.DROPPED, enrollment.getStatus());
        verify(auditLogService).success(eq("ENROLLMENT_DROP"), eq("Enrollment"), anyString(), any());
    }

    @Test
    void dropNonExistentEnrollment() {
        when(enrollmentRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> enrollmentService.drop(999L));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void deleteEnrollmentSuccessfully() {
        when(enrollmentRepository.existsById(1L)).thenReturn(true);

        enrollmentService.delete(1L);

        verify(enrollmentRepository).deleteById(1L);
        verify(auditLogService).success(eq("ENROLLMENT_DELETE"), eq("Enrollment"), anyString(), any());
    }

    @Test
    void deleteNonExistentEnrollment() {
        when(enrollmentRepository.existsById(999L)).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () -> enrollmentService.delete(999L));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void setGradeSuccessfully() {
        Enrollment enrollment = testEnrollment();

        GradeRequest gradeRequest = new GradeRequest(new BigDecimal("80"), new BigDecimal("90"));

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        EnrollmentResponse response = enrollmentService.setGrade(1L, gradeRequest);

        assertNotNull(response);
        assertNotNull(response.midtermGrade());
        assertNotNull(response.finalGrade());
        verify(auditLogService).success(eq("ENROLLMENT_GRADE"), eq("Enrollment"), anyString(), anyString());
    }

    @Test
    void setGradeForNonExistentEnrollment() {
        GradeRequest gradeRequest = new GradeRequest(new BigDecimal("80"), new BigDecimal("90"));

        when(enrollmentRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> enrollmentService.setGrade(999L, gradeRequest));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void listEnrollmentsByStudentSuccessfully() {
        Enrollment enrollment1 = testEnrollment();
        Enrollment enrollment2 = testEnrollment();
        enrollment2.setId(2L);

        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(enrollment1, enrollment2));

        List<EnrollmentResponse> responses = enrollmentService.listByStudent(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    void listEnrollmentsByCourseSuccessfully() {
        Enrollment enrollment1 = testEnrollment();
        Enrollment enrollment2 = testEnrollment();
        enrollment2.setId(2L);

        when(enrollmentRepository.findByCourseId(1L)).thenReturn(List.of(enrollment1, enrollment2));

        List<EnrollmentResponse> responses = enrollmentService.listByCourse(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }
}
