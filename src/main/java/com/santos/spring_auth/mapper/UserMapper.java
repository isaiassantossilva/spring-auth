package com.santos.spring_auth.mapper;

import com.santos.spring_auth.dto.user.UserRegistrationRequestDTO;
import com.santos.spring_auth.dto.user.UserResponseDTO;
import com.santos.spring_auth.entity.UserEntity;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    UserEntity toEntity(UserRegistrationRequestDTO request);

    UserResponseDTO toDTO(UserEntity user);
}
