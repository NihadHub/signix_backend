package com.signix.controller;

import com.signix.dto.SignDocumentRequest;
import com.signix.dto.SigningRequestResponse;
import com.signix.model.SigningRequest;
import com.signix.service.SigningRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sign")
public class SignController {
    private final SigningRequestService signingRequestService;

    @GetMapping("/{token}")
    public ResponseEntity<SigningRequestResponse> viewDocument(@PathVariable String token){
        SigningRequestResponse signingRequestResponse= signingRequestService.getSigningRequestByToken(token);
        return ResponseEntity.ok(signingRequestResponse);
    }

    @PostMapping("/{token}")
    public ResponseEntity<SigningRequestResponse> sign(@PathVariable String token,@Valid @RequestBody SignDocumentRequest signDocumentRequest) throws IOException {
        SigningRequestResponse response= signingRequestService.signDocument(token,signDocumentRequest.getSignatureImageBase64());
        return ResponseEntity.ok(response);
    }
}
