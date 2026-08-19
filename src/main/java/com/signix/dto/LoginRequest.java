package com.signix.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequest {

    @NotBlank(message = "L'email est obligatoire")
    @NotBlank(message = "Format invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

}
