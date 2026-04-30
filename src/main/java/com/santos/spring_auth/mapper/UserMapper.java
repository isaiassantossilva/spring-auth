package com.santos.spring_auth.mapper;

import com.santos.spring_auth.dto.user.UserRegistrationRequest;
import com.santos.spring_auth.dto.user.UserResponse;
import com.santos.spring_auth.entity.User;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "todos", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserRegistrationRequest request);

    UserResponse toResponse(User user);
}
