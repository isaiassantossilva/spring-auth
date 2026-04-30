package com.santos.spring_auth.service;

import com.santos.spring_auth.dto.todo.TodoCreateRequest;
import com.santos.spring_auth.dto.todo.TodoResponse;
import com.santos.spring_auth.dto.todo.TodoUpdateRequest;
import com.santos.spring_auth.entity.Todo;
import com.santos.spring_auth.entity.User;
import com.santos.spring_auth.enumeration.Role;
import com.santos.spring_auth.exception.ForbiddenException;
import com.santos.spring_auth.exception.ResourceNotFoundException;
import com.santos.spring_auth.gateway.AuthenticatedUserGateway;
import com.santos.spring_auth.mapper.TodoMapper;
import com.santos.spring_auth.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;
    private final AuthenticatedUserGateway authenticatedUserGateway;

    @Transactional
    public TodoResponse create(TodoCreateRequest request) {
        User owner = this.authenticatedUserGateway.current();
        Todo todo = this.todoMapper.toEntity(request);
        Instant now = Instant.now();
        todo.setOwner(owner);
        todo.setCreatedAt(now);
        todo.setUpdatedAt(now);
        Todo saved = this.todoRepository.save(todo);
        log.info("Created todo id={} owner={}", saved.getId(), owner.getUsername());
        return this.todoMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> findAccessible() {
        User user = this.authenticatedUserGateway.current();
        List<Todo> todos = user.getRole() == Role.ADMIN
                ? this.todoRepository.findAll()
                : this.todoRepository.findAllByOwnerId(user.getId());
        return todos.stream().map(this.todoMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TodoResponse findById(Long id) {
        return this.todoMapper.toResponse(this.loadAccessible(id));
    }

    @Transactional
    public TodoResponse update(Long id, TodoUpdateRequest request) {
        Todo todo = this.loadAccessible(id);
        this.todoMapper.updateEntity(request, todo);
        todo.setUpdatedAt(Instant.now());
        return this.todoMapper.toResponse(this.todoRepository.save(todo));
    }

    @Transactional
    public void delete(Long id) {
        Todo todo = this.loadAccessible(id);
        this.todoRepository.delete(todo);
        log.info("Deleted todo id={}", id);
    }

    private Todo loadAccessible(Long id) {
        User user = this.authenticatedUserGateway.current();
        Todo todo = this.todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found: " + id));
        if (user.getRole() != Role.ADMIN && !todo.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenException("Cannot access todo " + id);
        }
        return todo;
    }
}
