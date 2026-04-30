package com.santos.spring_auth.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santos.spring_auth.entity.UserEntity;
import com.santos.spring_auth.enumeration.Role;
import com.santos.spring_auth.repository.TodoRepository;
import com.santos.spring_auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestBase {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TodoRepository todoRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected MockMvc mockMvc;

    protected MockMvc buildMockMvc() {
        if (this.mockMvc == null) {
            this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                    .apply(springSecurity())
                    .build();
        }
        return this.mockMvc;
    }

    protected UserEntity seedUser(String username, String email, String rawPassword, Role role) {
        UserEntity user = UserEntity.builder()
                .username(username)
                .email(email)
                .password(this.passwordEncoder.encode(rawPassword))
                .role(role)
                .build();
        return this.userRepository.save(user);
    }

    protected RequestPostProcessor authAs(String username, Role... roles) {
        List<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority(r.authority()))
                .toList();
        List<String> roleClaims = Arrays.stream(roles).map(Role::authority).toList();
        return jwt()
                .jwt(j -> j.subject(username).claim("roles", roleClaims))
                .authorities(authorities);
    }
}
