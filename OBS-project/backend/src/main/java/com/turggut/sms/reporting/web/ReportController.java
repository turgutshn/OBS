package com.turggut.sms.reporting.web;

import com.turggut.sms.reporting.domain.AuditLog;
import com.turggut.sms.reporting.domain.AuditLogRepository;
import com.turggut.sms.student.domain.StudentRepository;
import com.turggut.sms.shared.dto.PageResponse;
import com.turggut.sms.reporting.dto.SummaryReport;
import com.turggut.sms.reporting.dto.TranscriptResponse;
import com.turggut.sms.shared.exception.ApiException;
import com.turggut.sms.shared.security.AuthenticatedUser;
import com.turggut.sms.shared.security.SecurityUtils;
import com.turggut.sms.reporting.service.ReportService;
import com.turggut.sms.reporting.service.TranscriptService;
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
