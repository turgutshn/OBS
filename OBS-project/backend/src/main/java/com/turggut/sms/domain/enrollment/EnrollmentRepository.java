package com.turggut.sms.domain.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);

    Optional<Enrollment> findByStudentIdAndCourseIdAndSemester(Long studentId, Long courseId, String semester);

    boolean existsByStudentIdAndCourseIdAndSemester(Long studentId, Long courseId, String semester);

    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        LEFT JOIN FETCH c.teacher
        WHERE e.student.id = :studentId
        ORDER BY e.semester DESC, c.code ASC
        """)
    List<Enrollment> findTranscriptByStudent(@Param("studentId") Long studentId);
}
