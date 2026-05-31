package com.turggut.sms.unit;

import com.turggut.sms.domain.fee.Fee;
import com.turggut.sms.domain.fee.FeeRepository;
import com.turggut.sms.domain.student.Student;
import com.turggut.sms.domain.student.StudentRepository;
import com.turggut.sms.domain.user.User;
import com.turggut.sms.dto.fee.FeeRequest;
import com.turggut.sms.dto.fee.FeeResponse;
import com.turggut.sms.exception.ApiException;
import com.turggut.sms.service.AuditLogService;
import com.turggut.sms.service.FeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeServiceUnitTest {

    @Mock
    private FeeRepository feeRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private FeeService feeService;

    private Student student() {
        User user = User.builder().id(1L).username("s1").email("s1@test.local").enabled(true).build();
        return Student.builder().id(5L).user(user).studentNumber("S1").firstName("A").lastName("B").build();
    }

    @Test
    void assessmentPastDueDateIsMarkedOverdue() {
        when(studentRepository.findById(5L)).thenReturn(Optional.of(student()));
        when(feeRepository.findByStudentIdAndSemester(5L, "2024-FALL")).thenReturn(Optional.empty());
        when(feeRepository.save(any(Fee.class))).thenAnswer(inv -> {
            Fee f = inv.getArgument(0);
            f.setId(99L);
            return f;
        });

        FeeRequest request = new FeeRequest(5L, "2024-FALL", new BigDecimal("1000.00"),
                LocalDate.now().minusDays(5), "late fee");
        FeeResponse response = feeService.createOrUpdateAssessment(request);

        assertEquals("OVERDUE", response.status());
        assertEquals(new BigDecimal("1000.00"), response.outstanding());
    }

    @Test
    void assessmentForMissingStudentThrows() {
        when(studentRepository.findById(404L)).thenReturn(Optional.empty());
        FeeRequest request = new FeeRequest(404L, "2024-FALL", new BigDecimal("100.00"),
                LocalDate.now().plusDays(5), null);
        assertThrows(ApiException.class, () -> feeService.createOrUpdateAssessment(request));
    }
}
