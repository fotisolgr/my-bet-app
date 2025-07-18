package com.mybetapp.controllers;

import com.mybetapp.dto.MatchDTO;
import com.mybetapp.models.SaveMatchRequest;
import com.mybetapp.services.MatchService;
import com.mybetapp.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bet")
public class MatchController {
	private static final Logger LOGGER = LoggerFactory.getLogger(MatchController.class);

	private final MatchService matchService;

	@GetMapping("/matches")
	public ResponseEntity<?> getPaginatedMatches(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "dateTime") String sortBy,
			@RequestParam(defaultValue = "desc") String direction) {
		LOGGER.info("Received request to fetch matches - page: {}, size: {}, sortBy: {}, direction: {}", page, size,
				sortBy, direction);

		Result<Page<MatchDTO>> result = matchService.getPaginatedMatches(page, size, sortBy, direction);

		if (result.isSuccess()) {
			return ResponseEntity.ok(result.getValue());
		} else {
			Map<String, String> error = Collections.singletonMap("error", result.getError());
			return ResponseEntity.status(500).body(error);
		}
	}

	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	@PostMapping("saveMatch")
	public ResponseEntity<?> saveMatch(@Valid @RequestBody SaveMatchRequest saveMatchRequest) {
		LOGGER.info("Received request to save match: {}", saveMatchRequest);

		Result<MatchDTO> result = matchService.saveMatch(saveMatchRequest);

		if (result.isSuccess()) {
			return ResponseEntity.status(HttpStatus.CREATED).body(result.getValue());
		}

		String error = result.getError().toLowerCase();

		Map<String, String> errorMap = Collections.singletonMap("error", result.getError());

		HttpStatus status;
		if (error.contains("already exists")) {
			status = HttpStatus.CONFLICT;
		} else if (error.contains("unauthorized:")) {
			status = HttpStatus.UNAUTHORIZED;
		} else {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return ResponseEntity.status(status).body(errorMap);
	}

	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	@PutMapping("updateMatch/{matchId}")
	public ResponseEntity<?> updateMatch(@PathVariable Long matchId,
			@Valid @RequestBody SaveMatchRequest updateRequest) {
		LOGGER.info("Received request to update match: {} with ID: {}", updateRequest, matchId);

		Result<MatchDTO> result = matchService.updateMatch(matchId, updateRequest);

		if (result.isSuccess()) {
			return ResponseEntity.ok(result.getValue());
		}

		String error = result.getError().toLowerCase();
		Map<String, String> errorMap = Collections.singletonMap("error", result.getError());

		HttpStatus status = error.contains("not found")
				? HttpStatus.NOT_FOUND
				: error.contains("duplicate")
						? HttpStatus.CONFLICT
						: error.contains("unauthorized")
								? HttpStatus.UNAUTHORIZED
								: error.contains("you do not have permission")
										? HttpStatus.FORBIDDEN
										: HttpStatus.INTERNAL_SERVER_ERROR;

		return ResponseEntity.status(status).body(errorMap);
	}

	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	@DeleteMapping("/matches/{matchId}")
	public ResponseEntity<?> deleteMatch(@PathVariable Long matchId) {
		LOGGER.info("Received request to delete match with id: {}", matchId);

		Result<Void> result = matchService.deleteMatch(matchId);

		if (result.isSuccess()) {
			return ResponseEntity.noContent().build(); // 204 No Content
		}

		String error = result.getError().toLowerCase();
		Map<String, String> errorMap = Collections.singletonMap("error", result.getError());

		HttpStatus status = error.contains("not_found")
				? HttpStatus.NOT_FOUND
				: error.contains("unauthorized")
						? HttpStatus.UNAUTHORIZED
						: error.contains("you do not have permission")
								? HttpStatus.FORBIDDEN
								: HttpStatus.INTERNAL_SERVER_ERROR;

		return ResponseEntity.status(status).body(errorMap); // 500
	}
}
