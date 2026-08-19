package com.signix.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.signix.model.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter @Getter @AllArgsConstructor @NoArgsConstructor
@Entity
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String filePath;

    private String signedFilePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status= DocumentStatus.DRAFT;

    @CreationTimestamp
    @Column(updatable  = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    private LocalDateTime signedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="owner_id", nullable = false)
    private User owner;

    @OneToOne( mappedBy = "document", cascade = CascadeType.ALL)
    private SigningRequest signingRequest;

    @OneToMany( mappedBy = "document", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<AuditLog> logs= new ArrayList<>();



}
