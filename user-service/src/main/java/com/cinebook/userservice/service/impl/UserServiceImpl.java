package com.cinebook.userservice.service.impl;

import com.cinebook.userservice.dto.AdminCreateUserRequest;
import com.cinebook.userservice.dto.AuthResponse;
import com.cinebook.userservice.dto.ChangePasswordRequest;
import com.cinebook.userservice.dto.LoginRequest;
import com.cinebook.userservice.dto.RegisterRequest;
import com.cinebook.userservice.dto.UpdateProfileRequest;
import com.cinebook.userservice.dto.UserResponse;
import com.cinebook.userservice.exception.ResourceAlreadyExistsException;
import com.cinebook.userservice.exception.ResourceNotFoundException;
import com.cinebook.userservice.exception.InvalidCredentialsException;
import com.cinebook.userservice.model.Role;
import com.cinebook.userservice.model.User;
import com.cinebook.userservice.repository.UserRepository;
import com.cinebook.userservice.security.JwtService;
import com.cinebook.userservice.service.UserService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Override
	public AuthResponse register(RegisterRequest request) {
		assertUserDoesNotExist(request.username(), request.email());

		User user = User.builder()
			.fullName(request.fullName())
			.email(request.email().toLowerCase())
			.username(request.username())
			.password(passwordEncoder.encode(request.password()))
			.roles(new HashSet<>(Set.of(Role.CUSTOMER)))
			.enabled(true)
			.build();

		User saved = userRepository.save(user);
		String token = jwtService.generateToken(saved);
		return new AuthResponse(token, "Bearer", toResponse(saved));
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByUsernameOrEmail(request.username(), request.username())
			.orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new InvalidCredentialsException("Invalid username or password");
		}

		String token = jwtService.generateToken(user);
		return new AuthResponse(token, "Bearer", toResponse(user));
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getCurrentUser(String username) {
		return toResponse(getUserByUsername(username));
	}

	@Override
	public UserResponse updateCurrentUser(String username, UpdateProfileRequest request) {
		User user = getUserByUsername(username);
		if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
			throw new ResourceAlreadyExistsException("Email already exists");
		}
		user.setFullName(request.fullName());
		user.setEmail(request.email().toLowerCase());
		return toResponse(userRepository.save(user));
	}

	@Override
	public UserResponse changePassword(String username, ChangePasswordRequest request) {
		User user = getUserByUsername(username);
		if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
			throw new InvalidCredentialsException("Current password is incorrect");
		}
		user.setPassword(passwordEncoder.encode(request.newPassword()));
		return toResponse(userRepository.save(user));
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserResponse> getAllUsers() {
		List<UserResponse> responses = new ArrayList<>();
		for (User user : userRepository.findAll()) {
			responses.add(toResponse(user));
		}
		return responses;
	}

	@Override
	public UserResponse createUserByAdmin(AdminCreateUserRequest request) {
		assertUserDoesNotExist(request.username(), request.email());

		User user = User.builder()
			.fullName(request.fullName())
			.email(request.email().toLowerCase())
			.username(request.username())
			.password(passwordEncoder.encode(request.password()))
			.roles(new HashSet<>(request.roles()))
			.enabled(true)
			.build();

		return toResponse(userRepository.save(user));
	}

	private void assertUserDoesNotExist(String username, String email) {
		if (userRepository.existsByUsername(username)) {
			throw new ResourceAlreadyExistsException("Username already exists");
		}
		if (userRepository.existsByEmail(email.toLowerCase())) {
			throw new ResourceAlreadyExistsException("Email already exists");
		}
	}

	private User getUserByUsername(String username) {
		return userRepository.findByUsername(username)
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(
			user.getId(),
			user.getFullName(),
			user.getEmail(),
			user.getUsername(),
			user.getRoles(),
			user.isEnabled(),
			user.getCreatedAt(),
			user.getUpdatedAt());
	}
}