package com.turggut.sms.service;

import com.turggut.sms.domain.enrollment.Enrollment;
import com.turggut.sms.domain.enrollment.EnrollmentRepository;
import com.turggut.sms.domain.student.Student;
import com.turggut.sms.domain.student.StudentRepository;
import com.turggut.sms.dto.enrollment.EnrollmentResponse;
import com.turggut.sms.dto.report.TranscriptResponse;
import com.turggut.sms.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TranscriptService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public TranscriptResponse generate(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> ApiException.notFound("Student not found"));

        List<Enrollment> enrollments = enrollmentRepository.findTranscriptByStudent(studentId);

        // Group by semester (preserve order: semester DESC)
        Map<String, List<Enrollment>> bySemester = new LinkedHashMap<>();
        for (Enrollment e : enrollments) {
            bySemester.computeIfAbsent(e.getSemester(), k -> new ArrayList<>()).add(e);
        }

        List<TranscriptResponse.SemesterBlock> blocks = new ArrayList<>();
        BigDecimal totalWeightedPoints = BigDecimal.ZERO;
        int totalGradedCredits = 0;
        int totalCredits = 0;
        int completedCredits = 0;

        for (Map.Entry<String, List<Enrollment>> entry : bySemester.entrySet()) {
            BigDecimal semWeighted = BigDecimal.ZERO;
            int semGradedCredits = 0;
            int semCredits = 0;
            List<EnrollmentResponse> rows = new ArrayList<>();
            for (Enrollment e : entry.getValue()) {
                rows.add(EnrollmentResponse.from(e));
                int credits = e.getCourse().getCredits();
                semCredits += credits;
                totalCredits += credits;
                BigDecimal gp = GradeCalculator.gradePoint(e.getLetterGrade());
                if (gp != null) {
                    BigDecimal weighted = gp.multiply(BigDecimal.valueOf(credits));
                    semWeighted = semWeighted.add(weighted);
                    totalWeightedPoints = totalWeightedPoints.add(weighted);
                    semGradedCredits += credits;
                    totalGradedCredits += credits;
                    if (GradeCalculator.isPassing(e.getLetterGrade())) {
                        completedCredits += credits;
                    }
                }
            }
            BigDecimal semGpa = semGradedCredits == 0 ? null :
                    semWeighted.divide(BigDecimal.valueOf(semGradedCredits), 2, RoundingMode.HALF_UP);
            blocks.add(new TranscriptResponse.SemesterBlock(entry.getKey(), rows, semGpa, semCredits));
        }

        BigDecimal gpa = totalGradedCredits == 0 ? null :
                totalWeightedPoints.divide(BigDecimal.valueOf(totalGradedCredits), 2, RoundingMode.HALF_UP);

        auditLogService.success("TRANSCRIPT_GENERATE", "Student", studentId.toString(), null);

        return new TranscriptResponse(
                student.getId(),
                student.getStudentNumber(),
                student.getFullName(),
                student.getDepartment(),
                student.getEnrollmentYear(),
                blocks,
                gpa,
                totalCredits,
                completedCredits);
    }
}
