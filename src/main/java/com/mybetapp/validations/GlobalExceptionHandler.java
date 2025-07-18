package com.mybetapp.validations;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
		// Collect all validation errors
		Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream().collect(Collectors
				.toMap(FieldError::getField, FieldError::getDefaultMessage, (existing, replacement) -> existing // in
																												// case
																												// of
																												// duplicate
																												// keys
				));

		// Specific error responses for known validation messages
		if (errors.values().stream()
				.anyMatch(msg -> msg.toLowerCase().contains("team a and team b must be different"))) {
			return ResponseEntity.badRequest().body(Map.of("error", "Team A and Team B must be different"));
		}

		if (errors.values().stream()
				.anyMatch(msg -> msg.toLowerCase().contains("team a and team b must be different"))) {
			return ResponseEntity.badRequest().body(Map.of("error", "Team A and Team B must be different"));
		}

		return ResponseEntity.badRequest().body(errors);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
		String msg = ex.getMessage();
		if (msg != null && msg.toLowerCase().contains("invalid sport")) {
			return ResponseEntity.badRequest().body(Map.of("error", msg));
		}

		if (msg != null && msg.toLowerCase().contains("invalid specifier")) {
			return ResponseEntity.badRequest().body(Map.of("error", msg));
		}

		return ResponseEntity.badRequest().body(Map.of("error", "Invalid request"));
	}
}
