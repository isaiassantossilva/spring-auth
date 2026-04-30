package com.santos.spring_auth.controller;

import com.santos.spring_auth.dto.user.UserRegistrationRequest;
import com.santos.spring_auth.enumeration.Role;
import com.santos.spring_auth.support.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIT extends IntegrationTestBase {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        this.mvc = this.buildMockMvc();
    }

    @Test
    void anonymousCannotRegister() throws Exception {
        UserRegistrationRequest body = new UserRegistrationRequest("eve", "eve@example.com", "secret123", Role.USER);

        this.mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanRegisterAdmin() throws Exception {
        UserRegistrationRequest body = new UserRegistrationRequest("root2", "root2@example.com", "secret123", Role.ADMIN);

        this.mvc.perform(post("/users")
                        .with(this.authAs("admin", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.username").value("root2"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        assertThat(this.userRepository.existsByUsername("root2")).isTrue();
    }

    @Test
    void operatorCanRegisterUser() throws Exception {
        UserRegistrationRequest body = new UserRegistrationRequest("user1", "user1@example.com", "secret123", Role.USER);

        this.mvc.perform(post("/users")
                        .with(this.authAs("op", Role.OPERATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void operatorCannotRegisterOperator() throws Exception {
        UserRegistrationRequest body = new UserRegistrationRequest("op2", "op2@example.com", "secret123", Role.OPERATOR);

        this.mvc.perform(post("/users")
                        .with(this.authAs("op", Role.OPERATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());

        assertThat(this.userRepository.existsByUsername("op2")).isFalse();
    }

    @Test
    void operatorCannotRegisterAdmin() throws Exception {
        UserRegistrationRequest body = new UserRegistrationRequest("root3", "root3@example.com", "secret123", Role.ADMIN);

        this.mvc.perform(post("/users")
                        .with(this.authAs("op", Role.OPERATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void plainUserCannotRegister() throws Exception {
        UserRegistrationRequest body = new UserRegistrationRequest("user2", "user2@example.com", "secret123", Role.USER);

        this.mvc.perform(post("/users")
                        .with(this.authAs("alice", Role.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerWithInvalidPayloadReturns400() throws Exception {
        String invalid = """
                {"username":"ab","email":"not-an-email","password":"123","role":"USER"}
                """;

        this.mvc.perform(post("/users")
                        .with(this.authAs("admin", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void registerWithDuplicateUsernameReturns409() throws Exception {
        this.seedUser("dup", "dup@example.com", "secret123", Role.USER);
        UserRegistrationRequest body = new UserRegistrationRequest("dup", "other@example.com", "secret123", Role.USER);

        this.mvc.perform(post("/users")
                        .with(this.authAs("admin", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Username")));
    }

    @Test
    void adminCanListUsers() throws Exception {
        this.mvc.perform(get("/users").with(this.authAs("admin", Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void plainUserCannotListUsers() throws Exception {
        this.mvc.perform(get("/users").with(this.authAs("alice", Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void operatorCannotListUsers() throws Exception {
        this.mvc.perform(get("/users").with(this.authAs("op", Role.OPERATOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanFindUserById() throws Exception {
        var saved = this.seedUser("findme", "findme@example.com", "secret123", Role.USER);

        this.mvc.perform(get("/users/" + saved.getId()).with(this.authAs("admin", Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("findme"));
    }

    @Test
    void adminGetMissingUserReturns404() throws Exception {
        this.mvc.perform(get("/users/9999").with(this.authAs("admin", Role.ADMIN)))
                .andExpect(status().isNotFound());
    }
}
