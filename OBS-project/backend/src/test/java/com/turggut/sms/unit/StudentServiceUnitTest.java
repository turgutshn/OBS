package com.turggut.sms.unit;

import com.turggut.sms.iam.domain.Role;
import com.turggut.sms.iam.domain.User;
import com.turggut.sms.iam.domain.UserRepository;
import com.turggut.sms.reporting.service.AuditLogService;
import com.turggut.sms.shared.exception.ApiException;
import com.turggut.sms.student.domain.Student;
import com.turggut.sms.student.domain.StudentRepository;
import com.turggut.sms.student.dto.StudentRequest;
import com.turggut.sms.student.dto.StudentResponse;
import com.turggut.sms.student.service.StudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudentServiceUnitTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private StudentService studentService;

    private StudentRequest testStudentRequest() {
        return new StudentRequest(
                "STU001",
                "John",
                "Doe",
                "12345678901",
                "john@example.com",
                "password123",
                "1234567890",
                LocalDate.of(2000, 1, 1),
                "Engineering",
                2023,
                "123 Main St"
        );
    }

    private Student testStudent() {
        User user = User.builder()
                .id(1L)
                .username("stu001")
                .email("john@example.com")
                .role(Role.STUDENT)
                .enabled(true)
                .build();
        return Student.builder()
                .id(1L)
                .user(user)
                .studentNumber("STU001")
                .firstName("John")
                .lastName("Doe")
                .nationalId("12345678901")
                .phone("1234567890")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .department("Engineering")
                .enrollmentYear(2023)
                .address("123 Main St")
                .build();
    }

    @Test
    void createStudentSuccessfully() {
        StudentRequest request = testStudentRequest();
        Student student = testStudent();
        User user = student.getUser();

        when(studentRepository.existsByStudentNumber(request.studentNumber())).thenReturn(false);
        when(studentRepository.existsByNationalId(request.nationalId())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername("stu001")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentResponse response = studentService.create(request);

        assertNotNull(response);
        assertEquals("STU001", response.studentNumber());
        assertEquals("John", response.firstName());
        verify(auditLogService).success(eq("STUDENT_CREATE"), eq("Student"), anyString(), anyString());
    }

    @Test
    void createStudentWithDuplicateStudentNumber() {
        StudentRequest request = testStudentRequest();
        when(studentRepository.existsByStudentNumber("STU001")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> studentService.create(request));
        assertTrue(exception.getCode().contains("duplicate"));
    }

    @Test
    void createStudentWithDuplicateEmail() {
        StudentRequest request = testStudentRequest();
        when(studentRepository.existsByStudentNumber(request.studentNumber())).thenReturn(false);
        when(studentRepository.existsByNationalId(request.nationalId())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> studentService.create(request));
        assertTrue(exception.getCode().contains("duplicate"));
    }

    @Test
    void updateStudentSuccessfully() {
        Long studentId = 1L;
        StudentRequest request = testStudentRequest();
        Student student = testStudent();
        User user = student.getUser();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentRepository.existsByStudentNumber(request.studentNumber())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentResponse response = studentService.update(studentId, request);

        assertNotNull(response);
        assertEquals("STU001", response.studentNumber());
        verify(auditLogService).success(eq("STUDENT_UPDATE"), eq("Student"), anyString(), any());
    }

    @Test
    void updateNonExistentStudent() {
        Long studentId = 999L;
        StudentRequest request = testStudentRequest();

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> studentService.update(studentId, request));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void deleteStudentSuccessfully() {
        Long studentId = 1L;
        Student student = testStudent();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        studentService.delete(studentId);

        verify(studentRepository).delete(student);
        verify(userRepository).deleteById(student.getUser().getId());
        verify(auditLogService).success(eq("STUDENT_DELETE"), eq("Student"), anyString(), anyString());
    }

    @Test
    void deleteNonExistentStudent() {
        Long studentId = 999L;
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> studentService.delete(studentId));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void getStudentSuccessfully() {
        Long studentId = 1L;
        Student student = testStudent();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        StudentResponse response = studentService.get(studentId);

        assertNotNull(response);
        assertEquals("STU001", response.studentNumber());
    }

    @Test
    void getNonExistentStudent() {
        Long studentId = 999L;
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> studentService.get(studentId));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void getStudentByUserIdSuccessfully() {
        Long userId = 1L;
        Student student = testStudent();

        when(studentRepository.findByUserId(userId)).thenReturn(Optional.of(student));

        StudentResponse response = studentService.getByUserId(userId);

        assertNotNull(response);
        assertEquals("STU001", response.studentNumber());
    }

    @Test
    void getStudentByNonExistentUserId() {
        Long userId = 999L;
        when(studentRepository.findByUserId(userId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> studentService.getByUserId(userId));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void searchStudentsSuccessfully() {
        Student student1 = testStudent();
        Student student2 = testStudent();
        student2.setId(2L);
        student2.setStudentNumber("STU002");

        Page<Student> page = new PageImpl<>(List.of(student1, student2));
        when(studentRepository.search("John", Pageable.unpaged())).thenReturn(page);

        com.turggut.sms.shared.dto.PageResponse<StudentResponse> response = studentService.search("John", Pageable.unpaged());

        assertNotNull(response);
        assertNotNull(response.content());
    }
}
