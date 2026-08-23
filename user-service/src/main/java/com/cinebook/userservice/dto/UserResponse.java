package com.cinebook.userservice.dto;

import java.time.Instant;
import java.util.Set;

import com.cinebook.userservice.model.Role;

public record UserResponse(
	Long id,
	String fullName,
	String email,
	String username,
	Set<Role> roles,
	boolean enabled,
	Instant createdAt,
	Instant updatedAt
) {
}