package com.signix.repository;

import com.signix.model.AuditLog;
import com.signix.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
    Page<AuditLog> findAuditLogsByDocumentOrderByTimestamp(Document document, Pageable pageable);
}
