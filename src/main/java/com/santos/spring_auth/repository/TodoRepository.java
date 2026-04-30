package com.santos.spring_auth.repository;

import com.santos.spring_auth.entity.TodoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<TodoEntity, Long> {

    List<TodoEntity> findAllByOwnerId(Long ownerId);

    Optional<TodoEntity> findByIdAndOwnerId(Long id, Long ownerId);
}
