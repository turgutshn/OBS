package com.turggut.sms.controller;

import com.turggut.sms.domain.fee.FeeStatus;
import com.turggut.sms.domain.student.StudentRepository;
import com.turggut.sms.dto.fee.FeeRequest;
import com.turggut.sms.dto.fee.FeeResponse;
import com.turggut.sms.dto.fee.PaymentRequest;
import com.turggut.sms.exception.ApiException;
import com.turggut.sms.security.AuthenticatedUser;
import com.turggut.sms.security.SecurityUtils;
import com.turggut.sms.service.FeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;
    private final StudentRepository studentRepository;

    @GetMapping("/admin/fees")
    @PreAuthorize("hasRole('ADMIN')")
    public List<FeeResponse> list(@RequestParam(required = false) FeeStatus status) {
        return status == null ? feeService.listAll() : feeService.listByStatus(status);
    }

    @PostMapping("/admin/fees")
    @PreAuthorize("hasRole('ADMIN')")
    public FeeResponse assess(@Valid @RequestBody FeeRequest request) {
        return feeService.createOrUpdateAssessment(request);
    }

    @PostMapping("/admin/fees/{id}/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public FeeResponse pay(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        return feeService.pay(id, request);
    }

    @PostMapping("/admin/fees/{id}/waive")
    @PreAuthorize("hasRole('ADMIN')")
    public FeeResponse waive(@PathVariable Long id) {
        return feeService.waive(id);
    }

    @DeleteMapping("/admin/fees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        feeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/students/{studentId}/fees")
    @PreAuthorize("hasRole('ADMIN')")
    public List<FeeResponse> listByStudent(@PathVariable Long studentId) {
        return feeService.listByStudent(studentId);
    }

    @GetMapping("/student/fees")
    @PreAuthorize("hasRole('STUDENT')")
    public List<FeeResponse> myFees() {
        AuthenticatedUser user = SecurityUtils.currentUser()
                .orElseThrow(() -> ApiException.unauthorized("Not authenticated"));
        Long studentId = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> ApiException.notFound("Student profile not found")).getId();
        return feeService.listByStudent(studentId);
    }
}
