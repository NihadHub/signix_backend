package com.signix.mapper;

import com.signix.dto.AuthResponse;
import com.signix.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "token", ignore = true)
    AuthResponse toAuthResponse(User user);

}
