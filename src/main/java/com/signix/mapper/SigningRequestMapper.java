package com.signix.mapper;

import com.signix.dto.SigningRequestResponse;
import com.signix.model.SigningRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SigningRequestMapper {

    @Mapping(target = "documentTitle", source="document.title")
    @Mapping(target = "fileUrl", ignore = true)
    @Mapping(target = "expired", ignore = true)
    SigningRequestResponse toResponse(SigningRequest signingRequest);
}
