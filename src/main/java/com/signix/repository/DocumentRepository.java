package com.signix.repository;

import com.signix.model.Document;
import com.signix.model.User;
import com.signix.model.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document,Long> {
    Page<Document> findDocumentByOwner(User owner, Pageable pageable);
    Page<Document> findDocumentByOwnerAndStatus(User owner, DocumentStatus documentStatus, Pageable pageable);
    @Query("SELECT d FROM Document d WHERE d.owner = :owner " +
            "AND (:title IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:status IS NULL OR d.status = :status)")
    Page<Document> searchDocuments(
            @Param("owner") User owner,
            @Param("title") String title,
            @Param("status") DocumentStatus status,
            Pageable pageable
    );
}
