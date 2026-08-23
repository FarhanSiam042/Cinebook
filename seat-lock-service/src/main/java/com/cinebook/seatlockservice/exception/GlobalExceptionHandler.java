package com.cinebook.seatlockservice.exception;

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

	@ExceptionHandler(SeatUnavailableException.class)
	public ResponseEntity<Map<String, Object>> handleSeatUnavailable(SeatUnavailableException ex) {
		Map<String, Object> body = build(HttpStatus.CONFLICT, ex.getMessage());
		body.put("conflictingSeatIds", ex.getConflictingSeatIds());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

	@ExceptionHandler(HoldNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleHoldNotFound(HoldNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.GONE).body(build(HttpStatus.GONE, ex.getMessage()));
	}

	@ExceptionHandler(InvalidOperationException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidOperation(InvalidOperationException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(build(HttpStatus.BAD_REQUEST, ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
			.findFirst()
			.map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
			.orElse("Validation failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(build(HttpStatus.BAD_REQUEST, message));
	}

	private Map<String, Object> build(HttpStatus status, String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		return body;
	}
}
