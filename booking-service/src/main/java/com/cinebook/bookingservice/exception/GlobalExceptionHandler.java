package com.cinebook.bookingservice.exception;

import feign.FeignException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BookingNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleBookingNotFound(BookingNotFoundException exception, WebRequest request) {
		return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(ExternalServiceException.class)
	public ResponseEntity<ApiErrorResponse> handleExternalService(ExternalServiceException exception, WebRequest request) {
		return buildError(HttpStatus.BAD_GATEWAY, exception.getMessage(), request);
	}

	@ExceptionHandler(FeignException.class)
	public ResponseEntity<ApiErrorResponse> handleFeign(FeignException exception, WebRequest request) {
		return buildError(HttpStatus.BAD_GATEWAY, "Downstream service request failed: " + exception.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception, WebRequest request) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getField() + " " + error.getDefaultMessage())
				.orElse("Validation failed");
		return buildError(HttpStatus.BAD_REQUEST, message, request);
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
