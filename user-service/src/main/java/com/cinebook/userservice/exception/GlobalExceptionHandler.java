package com.cinebook.userservice.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceAlreadyExistsException.class)
	public ResponseEntity<Map<String, Object>> handleAlreadyExists(ResourceAlreadyExistsException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
		return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
			.findFirst()
			.map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
			.orElse("Validation failed");
		return build(HttpStatus.BAD_REQUEST, message);
	}

	private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		return ResponseEntity.status(status).body(body);
	}
}