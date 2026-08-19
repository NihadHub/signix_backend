package com.signix.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SendDocumentRequest {

    @NotBlank(message = "L'email du signataire est obligatoire")
    @Email(message = "Format d'email invalide")
    private String signerEmail;
}
