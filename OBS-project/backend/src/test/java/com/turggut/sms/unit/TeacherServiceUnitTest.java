package com.turggut.sms.unit;

import com.turggut.sms.iam.domain.Role;
import com.turggut.sms.iam.domain.User;
import com.turggut.sms.iam.domain.UserRepository;
import com.turggut.sms.reporting.service.AuditLogService;
import com.turggut.sms.shared.exception.ApiException;
import com.turggut.sms.teacher.domain.Teacher;
import com.turggut.sms.teacher.domain.TeacherRepository;
import com.turggut.sms.teacher.dto.TeacherRequest;
import com.turggut.sms.teacher.dto.TeacherResponse;
import com.turggut.sms.teacher.service.TeacherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceUnitTest {

    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private TeacherService teacherService;

    private TeacherRequest testTeacherRequest() {
        return new TeacherRequest(
                "EMP001",
                "Jane",
                "Smith",
                "jane@example.com",
                "password123",
                "Prof.",
                "Engineering",
                "5551234567"
        );
    }

    private Teacher testTeacher() {
        User user = User.builder()
                .id(1L)
                .username("emp001")
                .email("jane@example.com")
                .role(Role.TEACHER)
                .enabled(true)
                .build();
        return Teacher.builder()
                .id(1L)
                .user(user)
                .employeeNumber("EMP001")
                .firstName("Jane")
                .lastName("Smith")
                .title("Prof.")
                .department("Engineering")
                .phone("5551234567")
                .build();
    }

    @Test
    void createTeacherSuccessfully() {
        TeacherRequest request = testTeacherRequest();
        Teacher teacher = testTeacher();
        User user = teacher.getUser();

        when(teacherRepository.existsByEmployeeNumber(request.employeeNumber())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername("emp001")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);

        TeacherResponse response = teacherService.create(request);

        assertNotNull(response);
        assertEquals("EMP001", response.employeeNumber());
        assertEquals("Jane", response.firstName());
        verify(auditLogService).success(eq("TEACHER_CREATE"), eq("Teacher"), anyString(), anyString());
    }

    @Test
    void createTeacherWithDuplicateEmployeeNumber() {
        TeacherRequest request = testTeacherRequest();
        when(teacherRepository.existsByEmployeeNumber("EMP001")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> teacherService.create(request));
        assertTrue(exception.getMessage().contains("duplicate"));
    }

    @Test
    void createTeacherWithDuplicateEmail() {
        TeacherRequest request = testTeacherRequest();
        when(teacherRepository.existsByEmployeeNumber(request.employeeNumber())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> teacherService.create(request));
        assertTrue(exception.getMessage().contains("duplicate"));
    }

    @Test
    void updateTeacherSuccessfully() {
        Long teacherId = 1L;
        TeacherRequest request = testTeacherRequest();
        Teacher teacher = testTeacher();
        User user = teacher.getUser();

        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(teacherRepository.existsByEmployeeNumber(request.employeeNumber())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);

        TeacherResponse response = teacherService.update(teacherId, request);

        assertNotNull(response);
        assertEquals("EMP001", response.employeeNumber());
        verify(auditLogService).success(eq("TEACHER_UPDATE"), eq("Teacher"), anyString(), any());
    }

    @Test
    void updateNonExistentTeacher() {
        Long teacherId = 999L;
        TeacherRequest request = testTeacherRequest();

        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> teacherService.update(teacherId, request));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void deleteTeacherSuccessfully() {
        Long teacherId = 1L;
        Teacher teacher = testTeacher();

        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        teacherService.delete(teacherId);

        verify(teacherRepository).delete(teacher);
        verify(userRepository).deleteById(teacher.getUser().getId());
        verify(auditLogService).success(eq("TEACHER_DELETE"), eq("Teacher"), anyString(), any());
    }

    @Test
    void deleteNonExistentTeacher() {
        Long teacherId = 999L;
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> teacherService.delete(teacherId));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void getTeacherSuccessfully() {
        Long teacherId = 1L;
        Teacher teacher = testTeacher();

        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        TeacherResponse response = teacherService.get(teacherId);

        assertNotNull(response);
        assertEquals("EMP001", response.employeeNumber());
    }

    @Test
    void getNonExistentTeacher() {
        Long teacherId = 999L;
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> teacherService.get(teacherId));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void searchTeachersSuccessfully() {
        Teacher teacher1 = testTeacher();
        Teacher teacher2 = testTeacher();
        teacher2.setId(2L);
        teacher2.setEmployeeNumber("EMP002");

        Page<Teacher> page = new PageImpl<>(List.of(teacher1, teacher2));
        when(teacherRepository.search("Jane", Pageable.unpaged())).thenReturn(page);

        com.turggut.sms.shared.dto.PageResponse<TeacherResponse> response = teacherService.search("Jane", Pageable.unpaged());

        assertNotNull(response);
        assertNotNull(response.content());
    }
}
