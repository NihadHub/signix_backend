package com.signix.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Builder
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Entity
public class SigningRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String signerEmail;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String signatureImageBase64;

    private LocalDateTime signedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

}
