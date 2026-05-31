package com.turggut.sms.fee.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeeRepository extends JpaRepository<Fee, Long> {

    List<Fee> findByStudentId(Long studentId);

    Optional<Fee> findByStudentIdAndSemester(Long studentId, String semester);

    List<Fee> findByStatus(FeeStatus status);
}
