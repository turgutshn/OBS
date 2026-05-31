package com.turggut.sms.teacher.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByEmployeeNumber(String employeeNumber);

    Optional<Teacher> findByUserId(Long userId);

    boolean existsByEmployeeNumber(String employeeNumber);

    @Query("""
        SELECT t FROM Teacher t
        WHERE (:q IS NULL OR :q = ''
            OR LOWER(t.firstName) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(t.lastName)  LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(t.employeeNumber) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(t.department) LIKE LOWER(CONCAT('%', :q, '%')))
        """)
    Page<Teacher> search(@Param("q") String q, Pageable pageable);
}
