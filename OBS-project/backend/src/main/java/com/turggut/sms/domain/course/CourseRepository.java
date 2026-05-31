package com.turggut.sms.domain.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    boolean existsByCode(String code);

    List<Course> findByTeacherId(Long teacherId);

    @Query("""
        SELECT c FROM Course c
        WHERE (:q IS NULL OR :q = ''
            OR LOWER(c.code) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(c.department) LIKE LOWER(CONCAT('%', :q, '%')))
        """)
    Page<Course> search(@Param("q") String q, Pageable pageable);
}
