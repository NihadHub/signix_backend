package com.signix.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignDocumentRequest {
    @NotBlank(message = "La signature est obligatoire")
    private String signatureImageBase64;
}
