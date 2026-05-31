package com.turggut.sms.domain.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentNumber(String studentNumber);

    Optional<Student> findByUserId(Long userId);

    boolean existsByStudentNumber(String studentNumber);

    boolean existsByNationalId(String nationalId);

    @Query("""
        SELECT s FROM Student s
        WHERE (:q IS NULL OR :q = ''
            OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(s.lastName)  LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(s.studentNumber) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(s.department) LIKE LOWER(CONCAT('%', :q, '%')))
        """)
    Page<Student> search(@Param("q") String q, Pageable pageable);
}
