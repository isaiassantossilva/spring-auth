package com.santos.spring_auth.repository;

import com.santos.spring_auth.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findAllByOwnerId(Long ownerId);

    Optional<Todo> findByIdAndOwnerId(Long id, Long ownerId);
}
