package com.signix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Builder @AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String fullName;
}
