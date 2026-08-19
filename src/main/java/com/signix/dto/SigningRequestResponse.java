package com.signix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SigningRequestResponse {
    private String documentTitle;
    private String fileUrl;
    private boolean expired;
}
