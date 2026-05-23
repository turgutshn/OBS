package com.turggut.sms.domain.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:actor IS NULL OR :actor = '' OR LOWER(a.actorUsername) LIKE LOWER(CONCAT('%', :actor, '%')))
          AND (:action IS NULL OR :action = '' OR a.action = :action)
        ORDER BY a.occurredAt DESC
        """)
    Page<AuditLog> search(@Param("actor") String actor,
                          @Param("action") String action,
                          Pageable pageable);
}
