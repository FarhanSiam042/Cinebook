package com.cinebook.userservice.service;

import java.util.List;

import com.cinebook.userservice.dto.AdminCreateUserRequest;
import com.cinebook.userservice.dto.AuthResponse;
import com.cinebook.userservice.dto.ChangePasswordRequest;
import com.cinebook.userservice.dto.LoginRequest;
import com.cinebook.userservice.dto.RegisterRequest;
import com.cinebook.userservice.dto.UpdateProfileRequest;
import com.cinebook.userservice.dto.UserResponse;

public interface UserService {
	AuthResponse register(RegisterRequest request);

	AuthResponse login(LoginRequest request);

	UserResponse getCurrentUser(String username);

	UserResponse updateCurrentUser(String username, UpdateProfileRequest request);

	UserResponse changePassword(String username, ChangePasswordRequest request);

	List<UserResponse> getAllUsers();

	UserResponse createUserByAdmin(AdminCreateUserRequest request);
}