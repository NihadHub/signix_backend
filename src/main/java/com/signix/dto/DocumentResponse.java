package com.signix.dto;

import com.signix.model.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Builder
@Getter @Setter @AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private String title;
    private DocumentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime signedAt;
    private String signerEmail;
}
