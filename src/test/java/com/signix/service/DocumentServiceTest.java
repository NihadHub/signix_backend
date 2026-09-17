package com.signix.service;

import com.signix.dto.DocumentResponse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private DocumentMapper documentMapper;
    @InjectMocks
    private DocumentService documentService;
    @Mock
    private SigningRequestRepository signingRequestRepository;
    @TempDir
    Path tempDir;
    @Test
    //Vérifier que sendDocument() refuse d'envoyer un document qui n'est pas DRAFT.
    void sendDocument_shouldThrow_whenDocumentIsNotDraft (){
        User owner = User.builder()
                .id(1L)
                .email("test@signix.ma")
                .build();
        Document document= Document.builder()
                .id(1L)
                .owner(owner)
                .status(DocumentStatus.SENT)
                .build();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        assertThrows(InvalidStateException.class,() -> {
            documentService.sendDocument(owner, 1L, "signataire@email.com");
        });

    }
    @Test
    void sendDocument_shouldThrow_whenOwnerIsNotSame(){
        User owner = User.builder()
                .id(1L)
                .email("owner@signex.ma")
                .build();
        User owner2 = User.builder()
                .id(2L)
                .email("owner2@signex.ma")
                .build();
        Document document= Document.builder()
                .id(1L)
                .owner(owner2)
                .status(DocumentStatus.DRAFT)
                .build();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        assertThrows(UnauthorizedDocumentAccessException.class,()-> documentService.sendDocument(owner,1L,"signerEmail@signix.ma"));
    }

    @Test
    void sendDocument_shouldSucceed_whenDocumentIsDraftAndOwnerMatches(){
        User owner=User.builder()
                .id(1L)
                .email("owner@signex.ma")
                .build();
        Document document= Document.builder()
                .id(1L)
                .owner(owner)
                .status(DocumentStatus.DRAFT)
                .build();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(documentMapper.toResponse(document)).thenReturn(
                DocumentResponse.builder()
                        .id(1L)
                        .status(DocumentStatus.SENT)
                        .build()
        );
        DocumentResponse response= documentService.sendDocument(owner,1L,"signer@signix.com");
        assertEquals(DocumentStatus.SENT, document.getStatus());
        assertNotNull(document.getSentAt());
        verify(signingRequestRepository).save(any(SigningRequest.class));
        verify(documentRepository).save(document);
        verify(auditLogService).log(document, AuditAction.DOCUMENT_SENT, owner.getEmail());
    }

    @Test
    void uploadDocument_shouldSucceed_whenFileIsValidPdf() throws IOException {
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());

        User owner = User.builder().id(1L).email("owner@signix.ma").build();

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("contrat.pdf");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("contenu".getBytes()));

        Document savedDocument = Document.builder()
                .id(1L)
                .title("Contrat")
                .owner(owner)
                .status(DocumentStatus.DRAFT)
                .build();

        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);
        when(documentMapper.toResponse(savedDocument)).thenReturn(
                DocumentResponse.builder().id(1L).title("Contrat").status(DocumentStatus.DRAFT).build()
        );

        DocumentResponse response = documentService.uploadDocument(owner, "Contrat", file);

        assertNotNull(response);
        assertEquals("Contrat", response.getTitle());
        verify(auditLogService).log(any(Document.class), eq(AuditAction.DOCUMENT_CREATED), eq(owner.getEmail()));
    }

    @Test
    void deleteDraft_shouldSucceed_whenDocumentIsDraft() throws IOException {
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());

        User owner = User.builder().id(1L).email("owner@signix.ma").build();

        Document document = Document.builder()
                .id(1L)
                .owner(owner)
                .status(DocumentStatus.DRAFT)
                .filePath("fake.pdf")
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        documentService.deleteDraft(1L, owner);

        verify(documentRepository).delete(document);
    }

    @Test
    void deleteDraft_shouldThrow_whenDocumentIsNotDraft() {
        User owner = User.builder().id(1L).email("owner@signix.ma").build();

        Document document = Document.builder()
                .id(1L)
                .owner(owner)
                .status(DocumentStatus.SENT)
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        assertThrows(InvalidStateException.class, () -> documentService.deleteDraft(1L, owner));
        verify(documentRepository, never()).delete(any());
    }
}
