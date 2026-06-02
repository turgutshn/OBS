package com.turggut.sms.student.service;

import com.turggut.sms.reporting.service.AuditLogService;

import com.turggut.sms.student.domain.Student;
import com.turggut.sms.student.domain.StudentRepository;
import com.turggut.sms.iam.domain.Role;
import com.turggut.sms.iam.domain.User;
import com.turggut.sms.iam.domain.UserRepository;
import com.turggut.sms.shared.dto.PageResponse;
import com.turggut.sms.student.dto.StudentRequest;
import com.turggut.sms.student.dto.StudentResponse;
import com.turggut.sms.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional
    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByStudentNumber(request.studentNumber())) {
            throw ApiException.conflict("duplicate_student_number", "Student number already exists");
        }
        if (request.nationalId() != null && !request.nationalId().isBlank()
                && studentRepository.existsByNationalId(request.nationalId())) {
            throw ApiException.conflict("duplicate_national_id", "National ID already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("duplicate_email", "Email already in use");
        }

        String username = request.studentNumber().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsername(username)) {
            throw ApiException.conflict("duplicate_username", "Username already in use");
        }

        String rawPassword = request.password() != null && !request.password().isBlank()
                ? request.password() : generateInitialPassword();

        User user = User.builder()
                .username(username)
                .email(request.email())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(Role.STUDENT)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        Student student = Student.builder()
                .user(user)
                .studentNumber(request.studentNumber())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .nationalId(emptyToNull(request.nationalId()))
                .phone(emptyToNull(request.phone()))
                .dateOfBirth(request.dateOfBirth())
                .department(emptyToNull(request.department()))
                .enrollmentYear(request.enrollmentYear())
                .address(emptyToNull(request.address()))
                .build();
        student = studentRepository.save(student);

        auditLogService.success("STUDENT_CREATE", "Student", student.getId().toString(),
                "studentNumber=" + student.getStudentNumber());
        return StudentResponse.from(student);
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Student not found"));

        if (!student.getStudentNumber().equals(request.studentNumber())
                && studentRepository.existsByStudentNumber(request.studentNumber())) {
            throw ApiException.conflict("duplicate_student_number", "Student number already exists");
        }

        User user = student.getUser();
        if (!user.getEmail().equalsIgnoreCase(request.email())
                && userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("duplicate_email", "Email already in use");
        }
        user.setEmail(request.email());
        userRepository.save(user);

        student.setStudentNumber(request.studentNumber());
        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setNationalId(emptyToNull(request.nationalId()));
        student.setPhone(emptyToNull(request.phone()));
        student.setDateOfBirth(request.dateOfBirth());
        student.setDepartment(emptyToNull(request.department()));
        student.setEnrollmentYear(request.enrollmentYear());
        student.setAddress(emptyToNull(request.address()));

        auditLogService.success("STUDENT_UPDATE", "Student", student.getId().toString(), null);
        return StudentResponse.from(student);
    }

    @Transactional
    public void delete(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Student not found"));
        Long userId = student.getUser().getId();
        studentRepository.delete(student);
        userRepository.deleteById(userId);
        auditLogService.success("STUDENT_DELETE", "Student", id.toString(),
                "studentNumber=" + student.getStudentNumber());
    }

    @Transactional(readOnly = true)
    public StudentResponse get(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Student not found"));
        return StudentResponse.from(student);
    }

    @Transactional(readOnly = true)
    public StudentResponse getByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("Student profile not found"));
        return StudentResponse.from(student);
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> search(String q, Pageable pageable) {
        return PageResponse.from(studentRepository.search(q, pageable), StudentResponse::from);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Student not found"));
        student.getUser().setEnabled(active);
        userRepository.save(student.getUser());
        auditLogService.success(active ? "STUDENT_ACTIVATE" : "STUDENT_DEACTIVATE",
                "Student", id.toString(), null);
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
