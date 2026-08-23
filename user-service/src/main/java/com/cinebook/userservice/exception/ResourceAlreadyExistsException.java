package com.cinebook.userservice.exception;

public class ResourceAlreadyExistsException extends RuntimeException {
	public ResourceAlreadyExistsException(String message) {
		super(message);
	}
}