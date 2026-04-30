package com.santos.spring_auth.controller;

import com.santos.spring_auth.dto.auth.LoginRequest;
import com.santos.spring_auth.enumeration.Role;
import com.santos.spring_auth.support.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIT extends IntegrationTestBase {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        this.mvc = this.buildMockMvc();
        this.seedUser("alice", "alice@example.com", "secret123", Role.USER);
    }

    @Test
    void loginWithValidCredentialsReturnsBearerToken() throws Exception {
        LoginRequest body = new LoginRequest("alice", "secret123");

        this.mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresInSeconds").value(60L * 60L));
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        LoginRequest body = new LoginRequest("alice", "wrong-password");

        this.mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void loginWithUnknownUserReturns401() throws Exception {
        LoginRequest body = new LoginRequest("ghost", "whatever");

        this.mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void loginWithBlankFieldsReturns400() throws Exception {
        LoginRequest body = new LoginRequest("", "");

        this.mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void meWithoutTokenReturns401() throws Exception {
        this.mvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meWithRealLoginTokenReturnsCurrentUser() throws Exception {
        LoginRequest body = new LoginRequest("alice", "secret123");
        MvcResult result = this.mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        String token = this.objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
        assertThat(token).isNotBlank();

        this.mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void meWithGarbageTokenReturns401() throws Exception {
        this.mvc.perform(get("/auth/me").header("Authorization", "Bearer not-a-real-jwt"))
                .andExpect(status().isUnauthorized());
    }
}
