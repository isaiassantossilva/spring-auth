package com.santos.spring_auth.mapper;

import com.santos.spring_auth.dto.todo.TodoCreateRequest;
import com.santos.spring_auth.dto.todo.TodoResponse;
import com.santos.spring_auth.dto.todo.TodoUpdateRequest;
import com.santos.spring_auth.entity.TodoEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TodoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completed", constant = "false")
    TodoEntity toEntity(TodoCreateRequest request);

    @Mapping(target = "ownerId", source = "owner.id")
    TodoResponse toResponse(TodoEntity todo);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(TodoUpdateRequest request, @MappingTarget TodoEntity todo);
}
