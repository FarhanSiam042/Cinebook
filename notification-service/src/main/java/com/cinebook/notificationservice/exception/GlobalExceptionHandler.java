package com.cinebook.notificationservice.exception;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NotificationNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(NotificationNotFoundException exception, WebRequest request) {
		return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, WebRequest request) {
		return buildError(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request);
	}

	private ResponseEntity<ApiErrorResponse> buildError(HttpStatus status, String message, WebRequest request) {
		String path = request.getDescription(false);
		if (path.startsWith("uri=")) {
			path = path.substring(4);
		}
		return ResponseEntity.status(status).body(new ApiErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path));
	}
}
