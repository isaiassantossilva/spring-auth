package com.santos.spring_auth.service;

import com.santos.spring_auth.dto.todo.TodoCreateRequest;
import com.santos.spring_auth.dto.todo.TodoResponse;
import com.santos.spring_auth.dto.todo.TodoUpdateRequest;
import com.santos.spring_auth.entity.TodoEntity;
import com.santos.spring_auth.entity.UserEntity;
import com.santos.spring_auth.enumeration.Role;
import com.santos.spring_auth.exception.ForbiddenException;
import com.santos.spring_auth.exception.ResourceNotFoundException;
import com.santos.spring_auth.gateway.AuthenticatedUserGateway;
import com.santos.spring_auth.mapper.TodoMapper;
import com.santos.spring_auth.repository.TodoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private TodoMapper todoMapper;

    @Mock
    private AuthenticatedUserGateway authenticatedUserGateway;

    @InjectMocks
    private TodoService todoService;

    private UserEntity user(Long id, Role role) {
        return UserEntity.builder().id(id).username("u" + id).email("u" + id + "@x").role(role).build();
    }

    private TodoEntity todo(Long id, UserEntity owner) {
        Instant now = Instant.now();
        return TodoEntity.builder().id(id).title("t").description("d").completed(false).createdAt(now).updatedAt(now).owner(owner).build();
    }

    @Test
    void createSetsOwnerAndTimestamps() {
        UserEntity owner = this.user(1L, Role.USER);
        TodoCreateRequest request = new TodoCreateRequest("title", "desc");
        TodoEntity mapped = TodoEntity.builder().title("title").description("desc").build();
        TodoEntity saved = this.todo(10L, owner);
        TodoResponse response = new TodoResponse(10L, "title", "desc", false, saved.getCreatedAt(), saved.getUpdatedAt(), 1L);

        when(this.authenticatedUserGateway.current()).thenReturn(owner);
        when(this.todoMapper.toEntity(request)).thenReturn(mapped);
        when(this.todoRepository.save(mapped)).thenReturn(saved);
        when(this.todoMapper.toResponse(saved)).thenReturn(response);

        TodoResponse result = this.todoService.create(request);

        assertThat(mapped.getOwner()).isSameAs(owner);
        assertThat(mapped.getCreatedAt()).isNotNull();
        assertThat(mapped.getUpdatedAt()).isNotNull();
        assertThat(result).isEqualTo(response);
    }

    @Test
    void findAccessibleAsAdminReturnsAll() {
        UserEntity admin = this.user(1L, Role.ADMIN);
        when(this.authenticatedUserGateway.current()).thenReturn(admin);
        when(this.todoRepository.findAll()).thenReturn(List.of(this.todo(1L, admin), this.todo(2L, this.user(99L, Role.USER))));
        when(this.todoMapper.toResponse(any())).thenReturn(new TodoResponse(0L, "", "", false, Instant.now(), Instant.now(), 0L));

        assertThat(this.todoService.findAccessible()).hasSize(2);
        verify(this.todoRepository).findAll();
        verify(this.todoRepository, never()).findAllByOwnerId(any());
    }

    @Test
    void findAccessibleAsUserReturnsOnlyOwnTodos() {
        UserEntity user = this.user(7L, Role.USER);
        when(this.authenticatedUserGateway.current()).thenReturn(user);
        when(this.todoRepository.findAllByOwnerId(7L)).thenReturn(List.of(this.todo(1L, user)));
        when(this.todoMapper.toResponse(any())).thenReturn(new TodoResponse(0L, "", "", false, Instant.now(), Instant.now(), 0L));

        assertThat(this.todoService.findAccessible()).hasSize(1);
        verify(this.todoRepository).findAllByOwnerId(7L);
        verify(this.todoRepository, never()).findAll();
    }

    @Test
    void findByIdAsOwnerSucceeds() {
        UserEntity user = this.user(7L, Role.USER);
        TodoEntity owned = this.todo(1L, user);
        TodoResponse response = new TodoResponse(1L, "t", "d", false, owned.getCreatedAt(), owned.getUpdatedAt(), 7L);
        when(this.authenticatedUserGateway.current()).thenReturn(user);
        when(this.todoRepository.findById(1L)).thenReturn(Optional.of(owned));
        when(this.todoMapper.toResponse(owned)).thenReturn(response);

        assertThat(this.todoService.findById(1L)).isEqualTo(response);
    }

    @Test
    void findByIdAsAdminCanAccessOthersTodo() {
        UserEntity admin = this.user(1L, Role.ADMIN);
        UserEntity other = this.user(7L, Role.USER);
        TodoEntity foreign = this.todo(2L, other);
        TodoResponse response = new TodoResponse(2L, "t", "d", false, foreign.getCreatedAt(), foreign.getUpdatedAt(), 7L);
        when(this.authenticatedUserGateway.current()).thenReturn(admin);
        when(this.todoRepository.findById(2L)).thenReturn(Optional.of(foreign));
        when(this.todoMapper.toResponse(foreign)).thenReturn(response);

        assertThat(this.todoService.findById(2L)).isEqualTo(response);
    }

    @Test
    void findByIdAsNonOwnerUserThrowsForbidden() {
        UserEntity intruder = this.user(2L, Role.USER);
        UserEntity owner = this.user(7L, Role.USER);
        when(this.authenticatedUserGateway.current()).thenReturn(intruder);
        when(this.todoRepository.findById(99L)).thenReturn(Optional.of(this.todo(99L, owner)));

        assertThatThrownBy(() -> this.todoService.findById(99L)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        UserEntity user = this.user(1L, Role.USER);
        when(this.authenticatedUserGateway.current()).thenReturn(user);
        when(this.todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.todoService.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAsNonOwnerThrowsForbidden() {
        UserEntity intruder = this.user(2L, Role.USER);
        UserEntity owner = this.user(7L, Role.USER);
        when(this.authenticatedUserGateway.current()).thenReturn(intruder);
        when(this.todoRepository.findById(5L)).thenReturn(Optional.of(this.todo(5L, owner)));

        TodoUpdateRequest request = new TodoUpdateRequest("new", "desc", true);
        assertThatThrownBy(() -> this.todoService.update(5L, request)).isInstanceOf(ForbiddenException.class);
        verify(this.todoRepository, never()).save(any());
    }

    @Test
    void deleteAsNonOwnerThrowsForbidden() {
        UserEntity intruder = this.user(2L, Role.USER);
        UserEntity owner = this.user(7L, Role.USER);
        when(this.authenticatedUserGateway.current()).thenReturn(intruder);
        when(this.todoRepository.findById(5L)).thenReturn(Optional.of(this.todo(5L, owner)));

        assertThatThrownBy(() -> this.todoService.delete(5L)).isInstanceOf(ForbiddenException.class);
        verify(this.todoRepository, never()).delete(any());
    }

    @Test
    void deleteAsAdminRemovesOthersTodo() {
        UserEntity admin = this.user(1L, Role.ADMIN);
        UserEntity owner = this.user(7L, Role.USER);
        TodoEntity foreign = this.todo(5L, owner);
        when(this.authenticatedUserGateway.current()).thenReturn(admin);
        when(this.todoRepository.findById(5L)).thenReturn(Optional.of(foreign));

        this.todoService.delete(5L);

        verify(this.todoRepository).delete(foreign);
    }
}
