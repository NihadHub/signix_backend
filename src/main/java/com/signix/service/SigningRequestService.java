package com.signix.service;

import com.signix.dto.SigningRequestResponse;
import com.signix.exception.AlreadySignedException;
import com.signix.exception.LinkExpiredException;
import com.signix.exception.SigningRequestNotFoundException;
import com.signix.mapper.SigningRequestMapper;
import com.signix.model.Document;
import com.signix.model.SigningRequest;
import com.signix.model.enums.AuditAction;
import com.signix.model.enums.DocumentStatus;
import com.signix.repository.DocumentRepository;
import com.signix.repository.SigningRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SigningRequestService {
    private final SigningRequestRepository signingRequestRepository;
    private final DocumentRepository documentRepository;
    private final SigningRequestMapper signingRequestMapper;
    private final PdfService pdfService;
    private final AuditLogService auditLogService;
    //Trouver la demande de signature correspondant à ce token dans bd.
    public SigningRequestResponse getSigningRequestByToken(String token){
        SigningRequest signingRequest= signingRequestRepository.findSigningRequestByToken(token).orElseThrow(() -> new SigningRequestNotFoundException(token) );
       checkExpiration(signingRequest);
        auditLogService.log(signingRequest.getDocument(),AuditAction.DOCUMENT_VIEWED,signingRequest.getSignerEmail());

        return signingRequestMapper.toResponse(signingRequest);
    }

    public SigningRequestResponse signDocument(String token, String signatureImageBase64 ) throws IOException {
        SigningRequest signingRequest= signingRequestRepository.findSigningRequestByToken(token).orElseThrow(()-> new SigningRequestNotFoundException(token));
        checkExpiration(signingRequest);
        if(signingRequest.getDocument().getStatus().equals(DocumentStatus.SIGNED)){
            throw new AlreadySignedException();
        }

        String signedFileName=pdfService.mergeSignatureIntoPdf(signingRequest.getDocument().getFilePath(),signatureImageBase64);
        signingRequest.setSignatureImageBase64(signatureImageBase64);
        signingRequest.setSignedAt(LocalDateTime.now());


        signingRequest.getDocument()
                .setSignedFilePath(signedFileName);
        signingRequest.getDocument().setStatus(DocumentStatus.SIGNED);
        signingRequest.getDocument().setSignedAt(LocalDateTime.now());

        signingRequestRepository.save(signingRequest);
        documentRepository.save(signingRequest.getDocument());

            auditLogService.log(signingRequest.getDocument(),AuditAction.DOCUMENT_SIGNED,signingRequest.getSignerEmail());
        return signingRequestMapper.toResponse(signingRequest);
    }

    private void checkExpiration(SigningRequest signingRequest){
        Document document= signingRequest.getDocument();
        if(LocalDateTime.now().isAfter(signingRequest.getExpirationDate()) && document.getStatus() != DocumentStatus.EXPIRED){
            document.setStatus(DocumentStatus.EXPIRED);
            documentRepository.save(document);
            auditLogService.log(signingRequest.getDocument(),AuditAction.DOCUMENT_EXPIRED,"SYSTEM");
            throw new LinkExpiredException();
        }
    }

}
