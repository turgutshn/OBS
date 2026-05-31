package com.turggut.sms.controller;

import com.turggut.sms.domain.audit.AuditLog;
import com.turggut.sms.domain.audit.AuditLogRepository;
import com.turggut.sms.domain.student.StudentRepository;
import com.turggut.sms.dto.PageResponse;
import com.turggut.sms.dto.report.SummaryReport;
import com.turggut.sms.dto.report.TranscriptResponse;
import com.turggut.sms.exception.ApiException;
import com.turggut.sms.security.AuthenticatedUser;
import com.turggut.sms.security.SecurityUtils;
import com.turggut.sms.service.ReportService;
import com.turggut.sms.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final TranscriptService transcriptService;
    private final StudentRepository studentRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping("/admin/reports/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public SummaryReport summary() {
        return reportService.summary();
    }

    @GetMapping("/admin/reports/transcript/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public TranscriptResponse transcript(@PathVariable Long studentId) {
        return transcriptService.generate(studentId);
    }

    @GetMapping("/student/transcript")
    @PreAuthorize("hasRole('STUDENT')")
    public TranscriptResponse myTranscript() {
        AuthenticatedUser user = SecurityUtils.currentUser()
                .orElseThrow(() -> ApiException.unauthorized("Not authenticated"));
        Long studentId = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> ApiException.notFound("Student profile not found")).getId();
        return transcriptService.generate(studentId);
    }

    @GetMapping("/admin/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<AuditLog> auditLogs(@RequestParam(required = false) String actor,
                                            @RequestParam(required = false) String action,
                                            @PageableDefault(size = 50) Pageable pageable) {
        return PageResponse.from(auditLogRepository.search(actor, action, pageable), a -> a);
    }
}
