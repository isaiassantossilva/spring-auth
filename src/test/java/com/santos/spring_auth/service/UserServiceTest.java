package com.santos.spring_auth.service;

import com.santos.spring_auth.dto.user.UserRegistrationRequest;
import com.santos.spring_auth.dto.user.UserResponse;
import com.santos.spring_auth.entity.UserEntity;
import com.santos.spring_auth.enumeration.Role;
import com.santos.spring_auth.exception.DuplicateResourceException;
import com.santos.spring_auth.exception.ResourceNotFoundException;
import com.santos.spring_auth.mapper.UserMapper;
import com.santos.spring_auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerEncodesPasswordAndPersistsUser() {
        UserRegistrationRequest request = new UserRegistrationRequest("alice", "alice@example.com", "secret123", Role.USER);
        UserEntity mapped = UserEntity.builder().username("alice").email("alice@example.com").role(Role.USER).build();
        UserEntity saved = UserEntity.builder().id(42L).username("alice").email("alice@example.com").role(Role.USER).build();
        UserResponse response = new UserResponse(42L, "alice", "alice@example.com", Role.USER);

        when(this.userRepository.existsByUsername("alice")).thenReturn(false);
        when(this.userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(this.userMapper.toEntity(request)).thenReturn(mapped);
        when(this.passwordEncoder.encode("secret123")).thenReturn("encoded-secret");
        when(this.userRepository.save(any(UserEntity.class))).thenReturn(saved);
        when(this.userMapper.toResponse(saved)).thenReturn(response);

        UserResponse result = this.userService.register(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(this.userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-secret");
        assertThat(result).isEqualTo(response);
    }

    @Test
    void registerThrowsWhenUsernameAlreadyExists() {
        UserRegistrationRequest request = new UserRegistrationRequest("alice", "alice@example.com", "secret123", Role.USER);
        when(this.userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> this.userService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username");
        verify(this.userRepository, never()).save(any());
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        UserRegistrationRequest request = new UserRegistrationRequest("alice", "alice@example.com", "secret123", Role.USER);
        when(this.userRepository.existsByUsername("alice")).thenReturn(false);
        when(this.userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> this.userService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");
        verify(this.userRepository, never()).save(any());
    }

    @Test
    void findByIdReturnsResponse() {
        UserEntity entity = UserEntity.builder().id(1L).username("alice").email("alice@example.com").role(Role.USER).build();
        UserResponse response = new UserResponse(1L, "alice", "alice@example.com", Role.USER);
        when(this.userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(this.userMapper.toResponse(entity)).thenReturn(response);

        assertThat(this.userService.findById(1L)).isEqualTo(response);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(this.userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllReturnsAllUsers() {
        UserEntity a = UserEntity.builder().id(1L).username("a").email("a@x").role(Role.USER).build();
        UserEntity b = UserEntity.builder().id(2L).username("b").email("b@x").role(Role.ADMIN).build();
        when(this.userRepository.findAll()).thenReturn(List.of(a, b));
        when(this.userMapper.toResponse(a)).thenReturn(new UserResponse(1L, "a", "a@x", Role.USER));
        when(this.userMapper.toResponse(b)).thenReturn(new UserResponse(2L, "b", "b@x", Role.ADMIN));

        assertThat(this.userService.findAll()).hasSize(2);
    }

    @Test
    void deleteRemovesExistingUser() {
        when(this.userRepository.existsById(1L)).thenReturn(true);

        this.userService.delete(1L);

        verify(this.userRepository).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(this.userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> this.userService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(this.userRepository, never()).deleteById(any());
    }
}
