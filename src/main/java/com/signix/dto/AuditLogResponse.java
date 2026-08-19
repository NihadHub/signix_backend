package com.signix.dto;

import com.signix.model.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
@Builder @AllArgsConstructor
@Getter
public class AuditLogResponse {
    private AuditAction action;
    private String actor;
    private LocalDateTime timestamp;
}
