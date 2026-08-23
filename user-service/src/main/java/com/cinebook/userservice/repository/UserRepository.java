package com.cinebook.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cinebook.userservice.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByUsernameOrEmail(String username, String email);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	List<User> findAllByEnabledTrue();
}