package com.turggut.sms.teacher.service;

import com.turggut.sms.reporting.service.AuditLogService;

import com.turggut.sms.teacher.domain.Teacher;
import com.turggut.sms.teacher.domain.TeacherRepository;
import com.turggut.sms.iam.domain.Role;
import com.turggut.sms.iam.domain.User;
import com.turggut.sms.iam.domain.UserRepository;
import com.turggut.sms.shared.dto.PageResponse;
import com.turggut.sms.teacher.dto.TeacherRequest;
import com.turggut.sms.teacher.dto.TeacherResponse;
import com.turggut.sms.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional
    public TeacherResponse create(TeacherRequest request) {
        if (teacherRepository.existsByEmployeeNumber(request.employeeNumber())) {
            throw ApiException.conflict("duplicate_employee_number", "Employee number already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("duplicate_email", "Email already in use");
        }
        String username = request.employeeNumber().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsername(username)) {
            throw ApiException.conflict("duplicate_username", "Username already in use");
        }

        String rawPassword = request.password() != null && !request.password().isBlank()
                ? request.password() : generateInitialPassword();

        User user = User.builder()
                .username(username)
                .email(request.email())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(Role.TEACHER)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        Teacher teacher = Teacher.builder()
                .user(user)
                .employeeNumber(request.employeeNumber())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .title(emptyToNull(request.title()))
                .department(emptyToNull(request.department()))
                .phone(emptyToNull(request.phone()))
                .build();
        teacher = teacherRepository.save(teacher);

        auditLogService.success("TEACHER_CREATE", "Teacher", teacher.getId().toString(),
                "employeeNumber=" + teacher.getEmployeeNumber());
        return TeacherResponse.from(teacher);
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Teacher not found"));

        if (!teacher.getEmployeeNumber().equals(request.employeeNumber())
                && teacherRepository.existsByEmployeeNumber(request.employeeNumber())) {
            throw ApiException.conflict("duplicate_employee_number", "Employee number already exists");
        }
        User user = teacher.getUser();
        if (!user.getEmail().equalsIgnoreCase(request.email())
                && userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("duplicate_email", "Email already in use");
        }
        user.setEmail(request.email());
        userRepository.save(user);

        teacher.setEmployeeNumber(request.employeeNumber());
        teacher.setFirstName(request.firstName());
        teacher.setLastName(request.lastName());
        teacher.setTitle(emptyToNull(request.title()));
        teacher.setDepartment(emptyToNull(request.department()));
        teacher.setPhone(emptyToNull(request.phone()));

        auditLogService.success("TEACHER_UPDATE", "Teacher", teacher.getId().toString(), null);
        return TeacherResponse.from(teacher);
    }

    @Transactional
    public void delete(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Teacher not found"));
        Long userId = teacher.getUser().getId();
        teacherRepository.delete(teacher);
        userRepository.deleteById(userId);
        auditLogService.success("TEACHER_DELETE", "Teacher", id.toString(), null);
    }

    @Transactional(readOnly = true)
    public TeacherResponse get(Long id) {
        return TeacherResponse.from(teacherRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Teacher not found")));
    }

    @Transactional(readOnly = true)
    public PageResponse<TeacherResponse> search(String q, Pageable pageable) {
        return PageResponse.from(teacherRepository.search(q, pageable), TeacherResponse::from);
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private String generateInitialPassword() {
        byte[] bytes = new byte[9];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes) + "A1!";
    }
}
