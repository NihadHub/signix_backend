package com.signix.service;

import com.signix.dto.DocumentResponse;
import com.signix.exception.DocumentNotFoundException;
import com.signix.exception.InvalidFileException;
import com.signix.exception.InvalidStateException;
import com.signix.exception.UnauthorizedDocumentAccessException;
import com.signix.mapper.DocumentMapper;
import com.signix.model.Document;
import com.signix.model.SigningRequest;
import com.signix.model.User;
import com.signix.model.enums.AuditAction;
import com.signix.model.enums.DocumentStatus;
import com.signix.repository.DocumentRepository;
import com.signix.repository.SigningRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {
    @Value("${app.upload.dir}")
    private String uploadDir;
    @Value("${app.signing.expiration-days}")
    private int expirationDays;
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final SigningRequestRepository signingRequestRepository;
    private final AuditLogService auditLogService;
    public DocumentResponse uploadDocument(User owner, String title, MultipartFile file) throws IOException {
        if(file==null || file.isEmpty()){
            throw new InvalidFileException("Le fichier est obligatoire");
        }

        if(!"application/pdf".equals(file.getContentType())){
            throw new InvalidFileException("Le fichier doit etre un pdf");
        }
        String originalFileName= file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;
        Path directory = Paths.get(uploadDir);
        if(!Files.exists(directory)){
            Files.createDirectories(directory);
        }
        Path filePath = Paths.get(uploadDir, uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Document document = Document.builder()
                .title(title)
                .filePath(uniqueFileName)
                .owner(owner)
                .build();
         Document savedDocument = documentRepository.save(document);

         auditLogService.log(document,AuditAction.DOCUMENT_CREATED,owner.getEmail());
        return documentMapper.toResponse(savedDocument);
    }

    public DocumentResponse sendDocument(User owner,Long documentId, String signerEmail ){
        Document document= documentRepository.findById(documentId).orElseThrow(() -> new DocumentNotFoundException(documentId));

        if(! document.getOwner().getId().equals(owner.getId())){
            throw new UnauthorizedDocumentAccessException();
        }
        if(!document.getStatus().equals(DocumentStatus.DRAFT) ){
            throw new InvalidStateException("On ne peut envoyer qu'un brouillon");
        }
        String token = UUID.randomUUID().toString();
        LocalDateTime experationDate = LocalDateTime.now().plusDays(expirationDays);

        SigningRequest signingRequest = SigningRequest.builder()
                .document(document)
                .signerEmail(signerEmail)
                .token(token)
                .expirationDate(experationDate)
                .build();
        signingRequestRepository.save(signingRequest);

        document.setStatus(DocumentStatus.SENT);
        document.setSentAt(LocalDateTime.now());
        documentRepository.save(document);

        auditLogService.log(document,AuditAction.DOCUMENT_SENT, owner.getEmail());
        return documentMapper.toResponse(document);

    }

    public Page<DocumentResponse > getUserDocuments(User owner, Pageable pageable){
             return   documentRepository.findDocumentByOwner(owner,pageable).map(documentMapper::toResponse);
    }

    public void deleteDraft (Long documentId, User owner) throws IOException {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new DocumentNotFoundException(documentId));
        if(!document.getOwner().getId().equals(owner.getId())){
            throw new UnauthorizedDocumentAccessException();
        }
        if(!document.getStatus().equals(DocumentStatus.DRAFT)){
            throw new InvalidStateException("On ne peut supprimer qu'un brouillon");

        }
        Path filePath = Paths.get(uploadDir, document.getFilePath());
        Files.deleteIfExists(filePath);
        documentRepository.delete(document);

    }

    public Resource downloadDocument (User owner, Long documentId) throws MalformedURLException {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new DocumentNotFoundException(documentId));
        if(!document.getOwner().getId().equals(owner.getId())){
            throw new UnauthorizedDocumentAccessException();
        }
        String fileName;
        if (document.getStatus() == DocumentStatus.SIGNED) {
            fileName = document.getSignedFilePath();
        } else {
            fileName = document.getFilePath();
        }
        Path filePath = Paths.get(uploadDir, fileName);
        return new UrlResource(filePath.toUri());
    }
    public Document getDocumentEntity(Long documentId, User owner){
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new DocumentNotFoundException(documentId));
        if(!document.getOwner().getId().equals(owner.getId())){
            throw new UnauthorizedDocumentAccessException();
        }
        return document;
    }
}
