package com.santos.spring_auth.service;

import com.santos.spring_auth.dto.user.UserRegistrationRequest;
import com.santos.spring_auth.dto.user.UserResponse;
import com.santos.spring_auth.entity.UserEntity;
import com.santos.spring_auth.exception.DuplicateResourceException;
import com.santos.spring_auth.exception.ResourceNotFoundException;
import com.santos.spring_auth.mapper.UserMapper;
import com.santos.spring_auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        if (this.userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already in use: " + request.username());
        }
        if (this.userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }
        UserEntity user = this.userMapper.toEntity(request);
        user.setPassword(this.passwordEncoder.encode(request.password()));
        UserEntity saved = this.userRepository.save(user);
        log.info("Registered user id={} username={} role={}", saved.getId(), saved.getUsername(), saved.getRole());
        return this.userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return this.userRepository.findAll().stream()
                .map(this.userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return this.userRepository.findById(id)
                .map(this.userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Transactional
    public void delete(Long id) {
        if (!this.userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        this.userRepository.deleteById(id);
        log.info("Deleted user id={}", id);
    }
}
