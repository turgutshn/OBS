package com.turggut.sms.fee.service;

import com.turggut.sms.reporting.service.AuditLogService;

import com.turggut.sms.fee.domain.Fee;
import com.turggut.sms.fee.domain.FeeRepository;
import com.turggut.sms.fee.domain.FeeStatus;
import com.turggut.sms.student.domain.Student;
import com.turggut.sms.student.domain.StudentRepository;
import com.turggut.sms.fee.dto.FeeRequest;
import com.turggut.sms.fee.dto.FeeResponse;
import com.turggut.sms.fee.dto.PaymentRequest;
import com.turggut.sms.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final FeeRepository feeRepository;
    private final StudentRepository studentRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public FeeResponse createOrUpdateAssessment(FeeRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> ApiException.notFound("Student not found"));

        Fee fee = feeRepository.findByStudentIdAndSemester(student.getId(), request.semester())
                .orElseGet(() -> Fee.builder()
                        .student(student)
                        .semester(request.semester())
                        .paidAmount(BigDecimal.ZERO)
                        .status(FeeStatus.PENDING)
                        .build());

        fee.setAmount(request.amount());
        fee.setDueDate(request.dueDate());
        fee.setDescription(request.description());
        recomputeStatus(fee);
        fee = feeRepository.save(fee);
        auditLogService.success("FEE_ASSESS", "Fee", fee.getId().toString(),
                "studentId=" + student.getId() + " semester=" + fee.getSemester() + " amount=" + fee.getAmount());
        return FeeResponse.from(fee);
    }

    @Transactional
    public FeeResponse pay(Long feeId, PaymentRequest payment) {
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> ApiException.notFound("Fee not found"));
        if (fee.getStatus() == FeeStatus.WAIVED) {
            throw ApiException.badRequest("fee_waived", "This fee has been waived");
        }
        BigDecimal newPaid = fee.getPaidAmount().add(payment.amount());
        if (newPaid.compareTo(fee.getAmount()) > 0) {
            throw ApiException.badRequest("overpayment", "Payment exceeds outstanding amount");
        }
        fee.setPaidAmount(newPaid);
        if (newPaid.compareTo(fee.getAmount()) == 0) {
            fee.setPaidAt(Instant.now());
        }
        recomputeStatus(fee);
        auditLogService.success("FEE_PAYMENT", "Fee", fee.getId().toString(),
                "amount=" + payment.amount() + " paidTotal=" + newPaid);
        return FeeResponse.from(fee);
    }

    @Transactional
    public FeeResponse waive(Long feeId) {
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> ApiException.notFound("Fee not found"));
        fee.setStatus(FeeStatus.WAIVED);
        auditLogService.success("FEE_WAIVE", "Fee", fee.getId().toString(), null);
        return FeeResponse.from(fee);
    }

    @Transactional
    public void delete(Long feeId) {
        if (!feeRepository.existsById(feeId)) {
            throw ApiException.notFound("Fee not found");
        }
        feeRepository.deleteById(feeId);
        auditLogService.success("FEE_DELETE", "Fee", feeId.toString(), null);
    }

    @Transactional(readOnly = true)
    public List<FeeResponse> listAll() {
        return feeRepository.findAll().stream().map(FeeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<FeeResponse> listByStudent(Long studentId) {
        return feeRepository.findByStudentId(studentId).stream().map(FeeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<FeeResponse> listByStatus(FeeStatus status) {
        return feeRepository.findByStatus(status).stream().map(FeeResponse::from).toList();
    }

    private void recomputeStatus(Fee fee) {
        if (fee.getStatus() == FeeStatus.WAIVED) return;
        BigDecimal paid = fee.getPaidAmount();
        if (paid.compareTo(BigDecimal.ZERO) == 0) {
            fee.setStatus(fee.getDueDate().isBefore(LocalDate.now()) ? FeeStatus.OVERDUE : FeeStatus.PENDING);
        } else if (paid.compareTo(fee.getAmount()) >= 0) {
            fee.setStatus(FeeStatus.PAID);
        } else {
            fee.setStatus(fee.getDueDate().isBefore(LocalDate.now()) ? FeeStatus.OVERDUE : FeeStatus.PARTIAL);
        }
    }
}
