package com.signix.service;

import com.signix.dto.SigningRequestResponse;
import com.signix.exception.AlreadySignedException;
import com.signix.exception.LinkExpiredException;
import com.signix.mapper.SigningRequestMapper;
import com.signix.model.Document;
import com.signix.model.SigningRequest;
import com.signix.model.enums.AuditAction;
import com.signix.model.enums.DocumentStatus;
import com.signix.repository.DocumentRepository;
import com.signix.repository.SigningRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SigningRequestServiceTest {

    @Mock
    private SigningRequestRepository signingRequestRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private SigningRequestMapper signingRequestMapper;
    @Mock
    private PdfService pdfService;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private SigningRequestService signingRequestService;

    @Test
    void signDocument_shouldSucceed_whenTokenValidAndNotSigned() throws IOException {
        Document document = Document.builder()
                .id(1L)
                .status(DocumentStatus.SENT)
                .filePath("original.pdf")
                .build();

        SigningRequest signingRequest = SigningRequest.builder()
                .id(1L)
                .document(document)
                .signerEmail("signer@email.com")
                .token("abc-token")
                .expirationDate(LocalDateTime.now().plusDays(2))
                .build();

        when(signingRequestRepository.findSigningRequestByToken("abc-token"))
                .thenReturn(Optional.of(signingRequest));
        when(pdfService.mergeSignatureIntoPdf("original.pdf", "base64signature"))
                .thenReturn("signed_original.pdf");
        when(signingRequestMapper.toResponse(signingRequest)).thenReturn(
                SigningRequestResponse.builder().documentTitle("Contrat").expired(false).build()
        );

        SigningRequestResponse response = signingRequestService.signDocument("abc-token", "base64signature");

        assertNotNull(response);
        assertEquals(DocumentStatus.SIGNED, document.getStatus());
        assertEquals("signed_original.pdf", document.getSignedFilePath());
        verify(auditLogService).log(document, AuditAction.DOCUMENT_SIGNED, "signer@email.com");
    }

    @Test
    void signDocument_shouldThrow_whenAlreadySigned() {
        Document document = Document.builder()
                .id(1L)
                .status(DocumentStatus.SIGNED)
                .build();

        SigningRequest signingRequest = SigningRequest.builder()
                .id(1L)
                .document(document)
                .token("abc-token")
                .expirationDate(LocalDateTime.now().plusDays(2))
                .build();

        when(signingRequestRepository.findSigningRequestByToken("abc-token"))
                .thenReturn(Optional.of(signingRequest));

        assertThrows(AlreadySignedException.class,
                () -> signingRequestService.signDocument("abc-token", "base64signature"));
    }

    @Test
    void signDocument_shouldThrow_whenLinkExpired() {
        Document document = Document.builder()
                .id(1L)
                .status(DocumentStatus.SENT)
                .build();

        SigningRequest signingRequest = SigningRequest.builder()
                .id(1L)
                .document(document)
                .token("abc-token")
                .expirationDate(LocalDateTime.now().minusDays(1))
                .build();

        when(signingRequestRepository.findSigningRequestByToken("abc-token"))
                .thenReturn(Optional.of(signingRequest));

        assertThrows(LinkExpiredException.class,
                () -> signingRequestService.signDocument("abc-token", "base64signature"));

        assertEquals(DocumentStatus.EXPIRED, document.getStatus());
        verify(auditLogService).log(document, AuditAction.DOCUMENT_EXPIRED, "SYSTEM");
    }
}
