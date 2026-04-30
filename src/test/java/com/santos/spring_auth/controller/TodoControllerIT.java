package com.santos.spring_auth.controller;

import com.santos.spring_auth.dto.todo.TodoCreateRequest;
import com.santos.spring_auth.dto.todo.TodoUpdateRequest;
import com.santos.spring_auth.entity.TodoEntity;
import com.santos.spring_auth.entity.UserEntity;
import com.santos.spring_auth.enumeration.Role;
import com.santos.spring_auth.support.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TodoControllerIT extends IntegrationTestBase {

    private MockMvc mvc;
    private UserEntity alice;
    private UserEntity bob;
    private UserEntity admin;

    @BeforeEach
    void setUp() {
        this.mvc = this.buildMockMvc();
        this.alice = this.seedUser("alice", "alice@example.com", "secret123", Role.USER);
        this.bob = this.seedUser("bob", "bob@example.com", "secret123", Role.USER);
        this.admin = this.seedUser("rootie", "rootie@example.com", "secret123", Role.ADMIN);
    }

    private TodoEntity seedTodo(UserEntity owner, String title) {
        Instant now = Instant.now();
        return this.todoRepository.save(TodoEntity.builder()
                .title(title)
                .description("desc")
                .completed(false)
                .createdAt(now)
                .updatedAt(now)
                .owner(owner)
                .build());
    }

    @Test
    void anonymousCannotAccessTodos() throws Exception {
        this.mvc.perform(get("/todos")).andExpect(status().isUnauthorized());
    }

    @Test
    void operatorCannotAccessTodos() throws Exception {
        this.mvc.perform(get("/todos").with(this.authAs("op", Role.OPERATOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanCreateOwnTodo() throws Exception {
        TodoCreateRequest body = new TodoCreateRequest("buy milk", "2L");

        this.mvc.perform(post("/todos")
                        .with(this.authAs("alice", Role.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("buy milk"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.ownerId").value(this.alice.getId()));
    }

    @Test
    void userListSeesOnlyOwnTodos() throws Exception {
        this.seedTodo(this.alice, "alice-task-1");
        this.seedTodo(this.alice, "alice-task-2");
        this.seedTodo(this.bob, "bob-task");

        this.mvc.perform(get("/todos").with(this.authAs("alice", Role.USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].ownerId", org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(this.alice.getId().intValue()))));
    }

    @Test
    void adminListSeesAllTodos() throws Exception {
        this.seedTodo(this.alice, "alice-task");
        this.seedTodo(this.bob, "bob-task");

        this.mvc.perform(get("/todos").with(this.authAs("rootie", Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void userCannotReadOthersTodo() throws Exception {
        TodoEntity bobs = this.seedTodo(this.bob, "bob-task");

        this.mvc.perform(get("/todos/" + bobs.getId()).with(this.authAs("alice", Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanReadOwnTodo() throws Exception {
        TodoEntity todo = this.seedTodo(this.alice, "alice-task");

        this.mvc.perform(get("/todos/" + todo.getId()).with(this.authAs("alice", Role.USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("alice-task"));
    }

    @Test
    void adminCanReadOthersTodo() throws Exception {
        TodoEntity bobs = this.seedTodo(this.bob, "bob-task");

        this.mvc.perform(get("/todos/" + bobs.getId()).with(this.authAs("rootie", Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("bob-task"));
    }

    @Test
    void userCannotUpdateOthersTodo() throws Exception {
        TodoEntity bobs = this.seedTodo(this.bob, "bob-task");
        TodoUpdateRequest body = new TodoUpdateRequest("hijacked", "desc", true);

        this.mvc.perform(put("/todos/" + bobs.getId())
                        .with(this.authAs("alice", Role.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());

        assertThat(this.todoRepository.findById(bobs.getId()).orElseThrow().getTitle()).isEqualTo("bob-task");
    }

    @Test
    void userCanUpdateOwnTodo() throws Exception {
        TodoEntity todo = this.seedTodo(this.alice, "alice-task");
        TodoUpdateRequest body = new TodoUpdateRequest("updated", "new desc", true);

        this.mvc.perform(put("/todos/" + todo.getId())
                        .with(this.authAs("alice", Role.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("updated"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void adminCanUpdateOthersTodo() throws Exception {
        TodoEntity todo = this.seedTodo(this.bob, "bob-task");
        TodoUpdateRequest body = new TodoUpdateRequest("admin-edit", "x", true);

        this.mvc.perform(put("/todos/" + todo.getId())
                        .with(this.authAs("rootie", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("admin-edit"));
    }

    @Test
    void userCannotDeleteOthersTodo() throws Exception {
        TodoEntity bobs = this.seedTodo(this.bob, "bob-task");

        this.mvc.perform(delete("/todos/" + bobs.getId()).with(this.authAs("alice", Role.USER)))
                .andExpect(status().isForbidden());

        assertThat(this.todoRepository.existsById(bobs.getId())).isTrue();
    }

    @Test
    void userCanDeleteOwnTodo() throws Exception {
        TodoEntity todo = this.seedTodo(this.alice, "alice-task");

        this.mvc.perform(delete("/todos/" + todo.getId()).with(this.authAs("alice", Role.USER)))
                .andExpect(status().isNoContent());

        assertThat(this.todoRepository.existsById(todo.getId())).isFalse();
    }

    @Test
    void getMissingTodoReturns404() throws Exception {
        this.mvc.perform(get("/todos/9999").with(this.authAs("alice", Role.USER)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithBlankTitleReturns400() throws Exception {
        String invalid = "{\"title\":\"\",\"description\":\"x\"}";

        this.mvc.perform(post("/todos")
                        .with(this.authAs("alice", Role.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray());
    }
}
