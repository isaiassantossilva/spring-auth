package com.santos.spring_auth.gateway;

import com.santos.spring_auth.entity.User;
import com.santos.spring_auth.exception.ForbiddenException;
import com.santos.spring_auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticatedUserGateway {

    private final UserRepository userRepository;

    public User current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("No authenticated user in context");
        }
        String username = authentication.getName();
        return this.userRepository.findByUsername(username)
                .orElseThrow(() -> new ForbiddenException("Authenticated user not found: " + username));
    }
}
