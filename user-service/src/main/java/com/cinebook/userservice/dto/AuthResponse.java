package com.cinebook.userservice.dto;

public record AuthResponse(
	String token,
	String tokenType,
	UserResponse user
) {
}