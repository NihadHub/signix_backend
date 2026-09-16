package com.signix.service;

import com.signix.dto.AuditLogResponse;
import com.signix.mapper.AuditLogMapper;
import com.signix.model.AuditLog;
import com.signix.model.Document;
import com.signix.model.enums.AuditAction;
import com.signix.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    public void log(Document document, AuditAction auditAction,String actor){
        AuditLog auditLog= AuditLog.builder()
                .document(document)
                .action(auditAction)
                .actor(actor)
                .build();
        auditLogRepository.save(auditLog);
    }

    public Page<AuditLogResponse> getDocumentHistory(Document document, Pageable pageable) {
        return auditLogRepository.findAuditLogsByDocumentOrderByTimestampDesc(document, pageable).map(auditLogMapper::toResponse);
    }
}
