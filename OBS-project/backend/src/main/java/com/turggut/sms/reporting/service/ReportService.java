package com.turggut.sms.reporting.service;

import com.turggut.sms.course.domain.CourseRepository;
import com.turggut.sms.enrollment.domain.EnrollmentRepository;
import com.turggut.sms.fee.domain.Fee;
import com.turggut.sms.fee.domain.FeeRepository;
import com.turggut.sms.student.domain.StudentRepository;
import com.turggut.sms.teacher.domain.TeacherRepository;
import com.turggut.sms.reporting.dto.SummaryReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FeeRepository feeRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public SummaryReport summary() {
        auditLogService.success("REPORT_SUMMARY", "Report", null, null);

        Map<String, Long> studentsByDept = studentRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDepartment() == null ? "Unassigned" : s.getDepartment(),
                        TreeMap::new, Collectors.counting()));

        Map<String, Long> coursesByDept = courseRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        c -> c.getDepartment() == null ? "Unassigned" : c.getDepartment(),
                        TreeMap::new, Collectors.counting()));

        var fees = feeRepository.findAll();
        BigDecimal assessed = fees.stream().map(Fee::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal collected = fees.stream().map(Fee::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outstanding = assessed.subtract(collected);

        Map<String, Long> feesByStatus = fees.stream()
                .collect(Collectors.groupingBy(f -> f.getStatus().name(),
                        TreeMap::new, Collectors.counting()));

        return new SummaryReport(
                studentRepository.count(),
                teacherRepository.count(),
                courseRepository.count(),
                enrollmentRepository.count(),
                assessed,
                collected,
                outstanding,
                studentsByDept,
                coursesByDept,
                feesByStatus);
    }
}
