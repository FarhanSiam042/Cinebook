package com.cinebook.userservice.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinebook.userservice.dto.AdminCreateUserRequest;
import com.cinebook.userservice.dto.ChangePasswordRequest;
import com.cinebook.userservice.dto.UpdateProfileRequest;
import com.cinebook.userservice.dto.UserResponse;
import com.cinebook.userservice.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

	private final UserService userService;

	@GetMapping("/me")
	@Operation(summary = "Get authenticated user profile")
	public ResponseEntity<UserResponse> me(Principal principal) {
		return ResponseEntity.ok(userService.getCurrentUser(principal.getName()));
	}

	@PutMapping("/me")
	@Operation(summary = "Update authenticated user profile")
	public ResponseEntity<UserResponse> updateMe(Principal principal, @Valid @RequestBody UpdateProfileRequest request) {
		return ResponseEntity.ok(userService.updateCurrentUser(principal.getName(), request));
	}

	@PutMapping("/me/password")
	@Operation(summary = "Change authenticated user password")
	public ResponseEntity<UserResponse> changePassword(Principal principal, @Valid @RequestBody ChangePasswordRequest request) {
		return ResponseEntity.ok(userService.changePassword(principal.getName(), request));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "List all users for admins")
	public ResponseEntity<List<UserResponse>> allUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create a user as admin")
	public ResponseEntity<UserResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUserByAdmin(request));
	}
}