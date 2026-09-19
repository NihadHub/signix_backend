package com.signix.mapper;

import com.signix.dto.DocumentResponse;
import com.signix.model.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "signerEmail", source = "signingRequest.signerEmail")
    @Mapping(target = "signingToken", source = "signingRequest.token")
    DocumentResponse toResponse(Document document);
}
