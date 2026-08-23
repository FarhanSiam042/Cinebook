package com.cinebook.userservice.dto;

import java.util.Set;

import com.cinebook.userservice.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record AdminCreateUserRequest(
	@NotBlank String fullName,
	@NotBlank @Email String email,
	@NotBlank String username,
	@NotBlank @Size(min = 8, max = 100) String password,
	@NotEmpty Set<Role> roles
) {
}