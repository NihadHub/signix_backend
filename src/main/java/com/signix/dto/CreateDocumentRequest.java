package com.signix.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class CreateDocumentRequest {
    @NotBlank(message = "Le titre est obligatoire")
    private String title;
    private MultipartFile file;
}
