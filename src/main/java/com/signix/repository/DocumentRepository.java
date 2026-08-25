package com.signix.repository;

import com.signix.model.Document;
import com.signix.model.User;
import com.signix.model.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document,Long> {
    Page<Document> findDocumentByOwner(User owner, Pageable pageable);
    Page<Document> findDocumentByOwnerAndStatus(User owner, DocumentStatus documentStatus, Pageable pageable);
}
