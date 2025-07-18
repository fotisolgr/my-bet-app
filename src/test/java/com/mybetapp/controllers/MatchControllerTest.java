package com.mybetapp.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.mybetapp.dto.MatchDTO;
import com.mybetapp.models.SaveMatchRequest;
import com.mybetapp.services.MatchService;
import com.mybetapp.util.Result;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class MatchControllerTest {

	@Mock
	private MatchService matchService;

	@InjectMocks
	private MatchController betController; // Replace with actual controller class name

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void getAllMatchesPaginated_success() {
		// Arrange
		int page = 0;
		int size = 10;
		String sortBy = "dateTime";
		String direction = "desc";

		Page<MatchDTO> mockPage = mock(Page.class); // mock the Page object or create a real PageImpl if you want

		Result<Page<MatchDTO>> successResult = Result.ok(mockPage);

		when(matchService.getPaginatedMatches(page, size, sortBy, direction)).thenReturn(successResult);

		// Act
		ResponseEntity<?> response = betController.getPaginatedMatches(page, size, sortBy, direction);

		// Assert
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getBody()).isEqualTo(mockPage);

		verify(matchService).getPaginatedMatches(page, size, sortBy, direction);
	}

	@Test
	void getAllMatchesPaginated_failure() {
		// Arrange
		int page = 0;
		int size = 10;
		String sortBy = "dateTime";
		String direction = "desc";

		String errorMessage = "Database error";
		Result<Page<MatchDTO>> failureResult = Result.error(errorMessage);

		when(matchService.getPaginatedMatches(page, size, sortBy, direction)).thenReturn(failureResult);

		// Act
		ResponseEntity<?> response = betController.getPaginatedMatches(page, size, sortBy, direction);

		// Assert
		assertThat(response.getStatusCodeValue()).isEqualTo(500);
		assertThat(response.getBody()).isInstanceOf(Map.class);
		Map<String, String> errorBody = (Map<String, String>) response.getBody();
		assertThat(errorBody).containsEntry("error", errorMessage);

		verify(matchService).getPaginatedMatches(page, size, sortBy, direction);
	}

	@Test
	void saveMatch_success() {
		SaveMatchRequest request = new SaveMatchRequest();
		MatchDTO mockMatchDTO = new MatchDTO();

		Result<MatchDTO> successResult = Result.ok(mockMatchDTO);

		when(matchService.saveMatch(request)).thenReturn(successResult);

		ResponseEntity<?> response = betController.saveMatch(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isEqualTo(mockMatchDTO);

		verify(matchService).saveMatch(request);
	}

	@Test
	void saveMatch_conflict() {
		SaveMatchRequest request = new SaveMatchRequest(/* valid data */ );
		String errorMsg = "Match already exists with that ID";

		Result<MatchDTO> failureResult = Result.error(errorMsg);

		when(matchService.saveMatch(request)).thenReturn(failureResult);

		ResponseEntity<?> response = betController.saveMatch(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).isInstanceOf(Map.class);

		Map<String, String> body = (Map<String, String>) response.getBody();
		assertThat(body).containsEntry("error", errorMsg);

		verify(matchService).saveMatch(request);
	}

	@Test
	void saveMatch_unauthorized() {
		SaveMatchRequest request = new SaveMatchRequest(/* valid data */ );
		String errorMsg = "Unauthorized: user token expired";

		Result<MatchDTO> failureResult = Result.error(errorMsg);

		when(matchService.saveMatch(request)).thenReturn(failureResult);

		ResponseEntity<?> response = betController.saveMatch(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).isInstanceOf(Map.class);

		Map<String, String> body = (Map<String, String>) response.getBody();
		assertThat(body).containsEntry("error", errorMsg);

		verify(matchService).saveMatch(request);
	}

	@Test
	void saveMatch_otherError() {
		SaveMatchRequest request = new SaveMatchRequest(/* valid data */ );
		String errorMsg = "Some other error occurred";

		Result<MatchDTO> failureResult = Result.error(errorMsg);

		when(matchService.saveMatch(request)).thenReturn(failureResult);

		ResponseEntity<?> response = betController.saveMatch(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isInstanceOf(Map.class);

		Map<String, String> body = (Map<String, String>) response.getBody();
		assertThat(body).containsEntry("error", errorMsg);

		verify(matchService).saveMatch(request);
	}

	@Test
	void updateMatch_success() {
		Long matchId = 123L;
		SaveMatchRequest updateRequest = new SaveMatchRequest(/* init with valid data */ );
		MatchDTO mockMatchDTO = new MatchDTO(/* init as needed */ );

		Result<MatchDTO> successResult = Result.ok(mockMatchDTO);
		when(matchService.updateMatch(matchId, updateRequest)).thenReturn(successResult);

		ResponseEntity<?> response = betController.updateMatch(matchId, updateRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(mockMatchDTO);

		verify(matchService).updateMatch(matchId, updateRequest);
	}

	@Test
	void updateMatch_notFound() {
		Long matchId = 123L;
		SaveMatchRequest updateRequest = new SaveMatchRequest(/* valid data */ );
		String errorMsg = "Match not found";

		Result<MatchDTO> failureResult = Result.error(errorMsg);
		when(matchService.updateMatch(matchId, updateRequest)).thenReturn(failureResult);

		ResponseEntity<?> response = betController.updateMatch(matchId, updateRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isInstanceOf(Map.class);

		Map<String, String> body = (Map<String, String>) response.getBody();
		assertThat(body).containsEntry("error", errorMsg);

		verify(matchService).updateMatch(matchId, updateRequest);
	}

	@Test
	void updateMatch_conflict() {
		Long matchId = 123L;
		SaveMatchRequest updateRequest = new SaveMatchRequest(/* valid data */ );
		String errorMsg = "Duplicate match entry";

		Result<MatchDTO> failureResult = Result.error(errorMsg);
		when(matchService.updateMatch(matchId, updateRequest)).thenReturn(failureResult);

		ResponseEntity<?> response = betController.updateMatch(matchId, updateRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).isInstanceOf(Map.class);

		Map<String, String> body = (Map<String, String>) response.getBody();
		assertThat(body).containsEntry("error", errorMsg);

		verify(matchService).updateMatch(matchId, updateRequest);
	}

	@Test
	void updateMatch_unauthorized() {
		Long matchId = 123L;
		SaveMatchRequest updateRequest = new SaveMatchRequest(/* valid data */ );
		String errorMsg = "Unauthorized access";

		Result<MatchDTO> failureResult = Result.error(errorMsg);
		when(matchService.updateMatch(matchId, updateRequest)).thenReturn(failureResult);

		ResponseEntity<?> response = betController.updateMatch(matchId, updateRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).isInstanceOf(Map.class);

		Map<String, String> body = (Map<String, String>) response.getBody();
		assertThat(body).containsEntry("error", errorMsg);

		verify(matchService).updateMatch(matchId, updateRequest);
	}

	@Test
	void updateMatch_forbidden() {
		Long matchId = 123L;
		SaveMatchRequest updateRequest = new SaveMatchRequest(/* valid data */ );
		String errorMsg = "You do not have permission to update this match";

		Result<MatchDTO> failureResult = Result.error(errorMsg);
		when(matchService.updateMatch(matchId, updateRequest)).thenReturn(failureResult);

		ResponseEntity<?> response = betController.updateMatch(matchId, updateRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(response.getBody()).isInstanceOf(Map.class);

		Map<String, String> body = (Map<String, String>) response.getBody();
		assertThat(body).containsEntry("error", errorMsg);

		verify(matchService).updateMatch(matchId, updateRequest);
	}

	@Test
	void updateMatch_otherError() {
		Long matchId = 123L;
		SaveMatchRequest updateRequest = new SaveMatchRequest(/* valid data */ );
		String errorMsg = "Unexpected server error";

		Result<MatchDTO> failureResult = Result.error(errorMsg);
		when(matchService.updateMatch(matchId, updateRequest)).thenReturn(failureResult);

		ResponseEntity<?> response = betController.updateMatch(matchId, updateRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isInstanceOf(Map.class);

		Map<String, String> body = (Map<String, String>) response.getBody();
		assertThat(body).containsEntry("error", errorMsg);

		verify(matchService).updateMatch(matchId, updateRequest);
	}

	@Test
	void deleteMatch_success() {
		Long matchId = 123L;
		Result<Void> successResult = Result.ok(null);

		when(matchService.deleteMatch(matchId)).thenReturn(successResult);

		ResponseEntity<?> response = betController.deleteMatch(matchId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();

		verify(matchService).deleteMatch(matchId);
	}

	@Test
	void deleteMatch_notFound() {
		Long matchId = 123L;
		String errorMsg = "not_found: match does not exist";
		Result<Void> failureResult = Result.error(errorMsg);

		when(matchService.deleteMatch(matchId)).thenReturn(failureResult);

		ResponseEntity<?> response = betController.deleteMatch(matchId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isInstanceOf(Map.class);

		Map<String, String> body = (Map<String, String>) response.getBody();
		assertThat(body).containsEntry("error", errorMsg);

		verify(matchService).deleteMatch(matchId);
	}

	@Test
	void deleteMatch_unauthorized() {
		Long matchId = 123L;
		String errorMsg = "Unauthorized access";
		Result<Void> failureResult = Result.error(errorMsg);

		when(matchService.deleteMatch(matchId)).thenReturn(failureResult);

		ResponseEntity<?> response = betController.deleteMatch(matchId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).isInstanceOf(Map.class);

		Map<String, String> body = (Map<String, String>) response.getBody();
		assertThat(body).containsEntry("error", errorMsg);

		verify(matchService).deleteMatch(matchId);
	}

	@Test
	void deleteMatch_forbidden() {
		Long matchId = 123L;
		String errorMsg = "You do not have permission to delete this match";
		Result<Void> failureResult = Result.error(errorMsg);

		when(matchService.deleteMatch(matchId)).thenReturn(failureResult);

		ResponseEntity<?> response = betController.deleteMatch(matchId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(response.getBody()).isInstanceOf(Map.class);

		Map<String, String> body = (Map<String, String>) response.getBody();
		assertThat(body).containsEntry("error", errorMsg);

		verify(matchService).deleteMatch(matchId);
	}

	@Test
	void deleteMatch_otherError() {
		Long matchId = 123L;
		String errorMsg = "Unexpected error occurred";
		Result<Void> failureResult = Result.error(errorMsg);

		when(matchService.deleteMatch(matchId)).thenReturn(failureResult);

		ResponseEntity<?> response = betController.deleteMatch(matchId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isInstanceOf(Map.class);

		Map<String, String> body = (Map<String, String>) response.getBody();
		assertThat(body).containsEntry("error", errorMsg);

		verify(matchService).deleteMatch(matchId);
	}
}
