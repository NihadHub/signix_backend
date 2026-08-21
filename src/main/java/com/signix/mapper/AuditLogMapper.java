package com.signix.mapper;

import com.signix.dto.AuditLogResponse;
import com.signix.model.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    AuditLogResponse toResponse(AuditLog auditLog);
}
