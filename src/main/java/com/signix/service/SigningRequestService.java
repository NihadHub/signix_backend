package com.signix.service;

import com.signix.dto.SigningRequestResponse;
import com.signix.exception.AlreadySignedException;
import com.signix.exception.LinkExpiredException;
import com.signix.exception.SigningRequestNotFoundException;
import com.signix.mapper.SigningRequestMapper;
import com.signix.model.AuditLog;
import com.signix.model.Document;
import com.signix.model.SigningRequest;
import com.signix.model.enums.AuditAction;
import com.signix.model.enums.DocumentStatus;
import com.signix.repository.AuditLogRepository;
import com.signix.repository.DocumentRepository;
import com.signix.repository.SigningRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SigningRequestService {
    private final SigningRequestRepository signingRequestRepository;
    private final DocumentRepository documentRepository;
    private final AuditLogRepository auditLogRepository;
    private final SigningRequestMapper signingRequestMapper;
    //Trouver la demande de signature correspondant à ce token dans bd.
    public SigningRequestResponse getSigningRequestByToken(String token){
        SigningRequest signingRequest= signingRequestRepository.findSigningRequestByToken(token).orElseThrow(() -> new SigningRequestNotFoundException(token) );
       checkExpiration(signingRequest);
        AuditLog auditLog = AuditLog.builder()
                .document(signingRequest.getDocument())
                .action(AuditAction.DOCUMENT_VIEWED)
                .actor(signingRequest.getSignerEmail())
                .build();
        auditLogRepository.save(auditLog);

        return signingRequestMapper.toResponse(signingRequest);
    }

    public SigningRequestResponse signDocument(String token, String signatureImageBase64 ){
        SigningRequest signingRequest= signingRequestRepository.findSigningRequestByToken(token).orElseThrow(()-> new SigningRequestNotFoundException(token));
        checkExpiration(signingRequest);
        if(signingRequest.getDocument().getStatus().equals(DocumentStatus.SIGNED)){
            throw new AlreadySignedException();
        }
        signingRequest.setSignatureImageBase64(signatureImageBase64);
        signingRequest.setSignedAt(LocalDateTime.now());
        signingRequestRepository.save(signingRequest);
        signingRequest.getDocument().setStatus(DocumentStatus.SIGNED);
        signingRequest.getDocument().setSignedAt(LocalDateTime.now());
        documentRepository.save(signingRequest.getDocument());
        AuditLog auditLog= AuditLog.builder()
                .document(signingRequest.getDocument())
                .action(AuditAction.DOCUMENT_SIGNED)
                .actor(signingRequest.getSignerEmail())
                .build();
        auditLogRepository.save(auditLog);
        return signingRequestMapper.toResponse(signingRequest);
    }

    private void checkExpiration(SigningRequest signingRequest){
        Document document= signingRequest.getDocument();
        if(LocalDateTime.now().isAfter(signingRequest.getExpirationDate()) && document.getStatus() != DocumentStatus.EXPIRED){
            document.setStatus(DocumentStatus.EXPIRED);
            documentRepository.save(document);
            AuditLog auditLog=  AuditLog.builder()
                    .document(signingRequest.getDocument())
                    .action(AuditAction.DOCUMENT_EXPIRED)
                    .actor("SYSTEM")
                    .build();
            auditLogRepository.save(auditLog);
            throw new LinkExpiredException();
        }
    }

}
