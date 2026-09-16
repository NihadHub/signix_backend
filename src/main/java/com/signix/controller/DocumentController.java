package com.signix.controller;

import com.signix.dto.AuditLogResponse;
import com.signix.dto.DocumentResponse;
import com.signix.dto.SendDocumentRequest;
import com.signix.model.Document;
import com.signix.model.User;
import com.signix.service.AuditLogService;
import com.signix.service.DocumentService;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

@RequiredArgsConstructor
@RestController
@RequestMapping
public class DocumentController {
    private final DocumentService documentService;
    private final AuditLogService auditLogService;
   @PostMapping
        public ResponseEntity<DocumentResponse> upload(@AuthenticationPrincipal User owner, @RequestParam  String title, @RequestParam MultipartFile file) throws IOException {
     DocumentResponse documentResponse= documentService.uploadDocument(owner,title,file);
       return ResponseEntity.status(HttpStatus.CREATED).body(documentResponse);
   }

   @PostMapping("/{id}/send")
    public ResponseEntity<DocumentResponse> send(@AuthenticationPrincipal User owner, @PathVariable Long id, @Valid @RequestBody SendDocumentRequest documentRequest){
       DocumentResponse documentResponse=documentService.sendDocument(owner,id,documentRequest.getSignerEmail());
       return ResponseEntity.ok(documentResponse);
   }

    @GetMapping
    public Page<DocumentResponse> userDocuments(@AuthenticationPrincipal User owner,  @RequestParam ( defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size   ){
       Pageable pageable= PageRequest.of(page,size);
        return documentService.getUserDocuments(owner,pageable);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User owner,@PathVariable Long id) throws IOException {
      documentService.deleteDraft(id, owner);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource>  download(@AuthenticationPrincipal User owner,@PathVariable Long id) throws MalformedURLException {
        Resource resource=documentService.downloadDocument(owner,id);

        return ResponseEntity.ok()
               .contentType(MediaType.APPLICATION_PDF)
               .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document.pdf\"")
               .body(resource);
    }
   @GetMapping("/{id}/history")
   public Page<AuditLogResponse> getHistory(
           @AuthenticationPrincipal User owner,
           @PathVariable Long id,
           @RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "10") int size){
   Pageable pageable= PageRequest.of(page, size);
    Document document= documentService.getDocumentEntity(id,owner);
    return auditLogService.getDocumentHistory(document, pageable);
   }
}
