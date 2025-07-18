package services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.Mockito.*;

import com.mybetapp.dto.MatchDTO;
import com.mybetapp.entities.Match;
import com.mybetapp.enums.Specifier;
import com.mybetapp.enums.Sport;
import com.mybetapp.models.MatchOdds;
import com.mybetapp.models.SaveMatchRequest;
import com.mybetapp.repositories.MatchRepository;
import com.mybetapp.services.MatchServiceImpl;
import com.mybetapp.util.Result;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class MatchServiceImplTest {

	@Mock
	private MatchRepository matchRepository;

	@InjectMocks
	private MatchServiceImpl matchService; // your service implementation class

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private void mockSecurityContextWithUsername(String username) {
		Jwt jwt = mock(Jwt.class);
		when(jwt.getClaimAsString("preferred_username")).thenReturn(username);

		Authentication authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(jwt);

		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		SecurityContextHolder.setContext(securityContext);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void saveMatch_currentUserPresent_success() {
		// Arrange
		mockSecurityContextWithUsername("currentUser");

		SaveMatchRequest request = createSaveMatchRequest();

		// Mock helper methods if needed
		MatchServiceImpl spyService = Mockito.spy(matchService);

		when(matchRepository.existsByTeamAAndTeamBAndMatchDateAndMatchTimeAndSport(anyString(), anyString(),
				any(LocalDate.class), any(LocalTime.class), any(Sport.class))).thenReturn(false);

		when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		Result<MatchDTO> result = spyService.saveMatch(request);

		// Assert
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.getValue()).isNotNull();
	}

	@Test
	void saveMatch_noCurrentUser_returnsUnauthorizedError() {
		// Clear security context to simulate no logged-in user
		SecurityContextHolder.clearContext();
		SaveMatchRequest request = createSaveMatchRequest();

		Result<MatchDTO> result = matchService.saveMatch(request);

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.getError()).containsIgnoringCase("unauthorized");
	}

	@Test
	void updateMatch_successfulUpdate() {
		Long matchId = 1L;
		String currentUser = "user1";

		SaveMatchRequest updateRequest = createSaveMatchRequest();

		Match existingMatch = new Match();
		existingMatch.setId(matchId);
		existingMatch.setOwner(currentUser);
		existingMatch.setTeamA("Old Team A");
		existingMatch.setTeamB("Old Team B");
		existingMatch.setMatchDate(LocalDate.of(2025, 1, 1));
		existingMatch.setMatchTime(LocalTime.of(15, 0));
		existingMatch.setSport(Sport.FOOTBALL);

		mockSecurityContextWithUsername(currentUser);

		when(matchRepository.findById(matchId)).thenReturn(Optional.of(existingMatch));

		// Spy on betService to mock internal methods
		MatchServiceImpl spyService = spy(matchService);

		when(matchRepository.save(existingMatch)).thenReturn(existingMatch);

		Result<MatchDTO> result = spyService.updateMatch(matchId, updateRequest);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.getValue()).isNotNull();

		verify(matchRepository).save(existingMatch);
	}

	@Test
	void updateMatch_matchNotFound() {
		Long matchId = 42L;
		SaveMatchRequest updateRequest = new SaveMatchRequest();

		when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

		Result<MatchDTO> result = matchService.updateMatch(matchId, updateRequest);

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.getError()).containsIgnoringCase("not found");
	}

	@Test
	void updateMatch_unauthorized_noUserInContext() {
		Long matchId = 1L;

		Match existingMatch = new Match();
		existingMatch.setId(matchId);
		existingMatch.setOwner("user1");

		when(matchRepository.findById(matchId)).thenReturn(Optional.of(existingMatch));

		SecurityContextHolder.clearContext(); // No user in context

		SaveMatchRequest updateRequest = new SaveMatchRequest();

		Result<MatchDTO> result = matchService.updateMatch(matchId, updateRequest);

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.getError()).containsIgnoringCase("unauthorized");
	}

	@Test
	void updateMatch_forbidden_userNotOwner() {
		Long matchId = 1L;
		String currentUser = "user2"; // Different user than owner

		Match existingMatch = new Match();
		existingMatch.setId(matchId);
		existingMatch.setOwner("user1");

		mockSecurityContextWithUsername(currentUser);

		when(matchRepository.findById(matchId)).thenReturn(Optional.of(existingMatch));

		SaveMatchRequest updateRequest = new SaveMatchRequest();

		Result<MatchDTO> result = matchService.updateMatch(matchId, updateRequest);

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.getError()).containsIgnoringCase("do not have permission");
	}

	@Test
	void deleteMatch_successfulDeletion() {
		Long matchId = 1L;
		String currentUser = "user1";

		Match match = new Match();
		match.setId(matchId);
		match.setOwner(currentUser);

		mockSecurityContextWithUsername(currentUser);

		when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
		doNothing().when(matchRepository).delete(match);

		Result<Void> result = matchService.deleteMatch(matchId);

		assertThat(result.isSuccess()).isTrue();
		verify(matchRepository).delete(match);
	}

	@Test
	void deleteMatch_matchNotFound() {
		Long matchId = 42L;

		when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

		Result<Void> result = matchService.deleteMatch(matchId);

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.getError()).containsIgnoringCase("not_found");
		verify(matchRepository, never()).delete(any());
	}

	@Test
	void deleteMatch_unauthorized_noUserInContext() {
		Long matchId = 1L;

		Match match = new Match();
		match.setId(matchId);
		match.setOwner("user1");

		when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

		SecurityContextHolder.clearContext(); // No user in context

		Result<Void> result = matchService.deleteMatch(matchId);

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.getError()).containsIgnoringCase("unauthorized");
		verify(matchRepository, never()).delete(any());
	}

	@Test
	void deleteMatch_forbidden_userNotOwner() {
		Long matchId = 1L;
		String currentUser = "user2"; // Different from owner

		Match match = new Match();
		match.setId(matchId);
		match.setOwner("user1");

		mockSecurityContextWithUsername(currentUser);

		when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

		Result<Void> result = matchService.deleteMatch(matchId);

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.getError()).containsIgnoringCase("do not have permission");
		verify(matchRepository, never()).delete(any());
	}

	@Test
	void deleteMatch_exceptionHandling() {
		Long matchId = 1L;
		String currentUser = "user1";

		Match match = new Match();
		match.setId(matchId);
		match.setOwner(currentUser);

		mockSecurityContextWithUsername(currentUser);

		when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
		doThrow(new RuntimeException("DB error")).when(matchRepository).delete(match);

		Result<Void> result = matchService.deleteMatch(matchId);

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.getError()).containsIgnoringCase("internal_error");
		verify(matchRepository).delete(match);
	}

	@Test
	void getMatchesPaginated_returnsPageOfMatchesSortedByOwnerAsc() {
		// Arrange
		int page = 0;
		int size = 2;
		String sortBy = "owner";
		String direction = "asc";

		Match match1 = createMatch(1L, "alice", Sport.BASKETBALL, LocalDate.now(), LocalTime.NOON);
		Match match2 = createMatch(2L, "bob", Sport.BASKETBALL, LocalDate.now().minusDays(1), LocalTime.NOON);

		List<Match> matches = List.of(match1, match2);
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("owner")));
		Page<Match> matchPage = new PageImpl<>(matches, pageable, matches.size());

		when(matchRepository.findAll(any(Pageable.class))).thenReturn(matchPage);

		// Act
		Result<Page<MatchDTO>> result = matchService.getPaginatedMatches(page, size, sortBy, direction);

		// Assert
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.getValue().getTotalElements()).isEqualTo(2);
		org.assertj.core.api.Assertions.assertThat(result.getValue().getContent()).extracting(MatchDTO::getMatchOwner)
				.containsExactly("alice", "bob");
	}

	@Test
	void getMatchesPaginated_returnsPageOfMatchesSortedBySportAsc() {
		// Arrange
		int page = 0;
		int size = 2;
		String sortBy = "sport";
		String direction = "asc";

		Match match1 = createMatch(1L, "alice", Sport.BASKETBALL, LocalDate.now(), LocalTime.NOON);
		Match match2 = createMatch(2L, "bob", Sport.FOOTBALL, LocalDate.now().minusDays(1), LocalTime.NOON);

		List<Match> matches = List.of(match1, match2);
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("sport")));
		Page<Match> matchPage = new PageImpl<>(matches, pageable, matches.size());

		when(matchRepository.findAll(any(Pageable.class))).thenReturn(matchPage);

		// Act
		Result<Page<MatchDTO>> result = matchService.getPaginatedMatches(page, size, sortBy, direction);

		// Assert
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.getValue().getTotalElements()).isEqualTo(2);
		org.assertj.core.api.Assertions.assertThat(result.getValue().getContent()).extracting(MatchDTO::getSport)
				.containsExactly(Sport.BASKETBALL, Sport.FOOTBALL);
	}

	@Test
	void getMatchesPaginated_returnsPageOfMatchesSortedByMatchDateAsc() {
		// Arrange
		int page = 0;
		int size = 2;
		String sortBy = "matchDate";
		String direction = "asc";

		Match match1 = createMatch(1L, "alice", Sport.BASKETBALL, LocalDate.now().minusDays(1), LocalTime.NOON);
		Match match2 = createMatch(2L, "bob", Sport.BASKETBALL, LocalDate.now(), LocalTime.of(14, 0));

		List<Match> matches = List.of(match1, match2);
		Sort sort = Sort.by(Sort.Order.asc("matchDate"), Sort.Order.asc("matchTime"));
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<Match> matchPage = new PageImpl<>(matches, pageable, matches.size());

		when(matchRepository.findAll(any(Pageable.class))).thenReturn(matchPage);

		// Act
		Result<Page<MatchDTO>> result = matchService.getPaginatedMatches(page, size, sortBy, direction);

		// Assert
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.getValue().getTotalElements()).isEqualTo(2);
		org.assertj.core.api.Assertions.assertThat(result.getValue().getContent())
				.extracting(MatchDTO::getMatchDate, MatchDTO::getMatchTime)
				.containsExactly(tuple(LocalDate.now().minusDays(1), LocalTime.NOON),
						tuple(LocalDate.now(), LocalTime.of(14, 0)));
	}

	@Test
	void getMatchesPaginated_handlesException() {
		when(matchRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("DB down"));

		Result<Page<MatchDTO>> result = matchService.getPaginatedMatches(0, 10, "sport", "desc");

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.getError()).containsIgnoringCase("failed to fetch paginated matches");
	}

	private SaveMatchRequest createSaveMatchRequest() {
		SaveMatchRequest request = new SaveMatchRequest();
		request.setMatchDate("2025-08-22");
		request.setMatchTime("13:52");
		request.setTeamA("AEK");
		request.setTeamB("PAO");
		request.setSport(Sport.BASKETBALL);

		MatchOdds matchOddWin = new MatchOdds();
		matchOddWin.setSpecifier(Specifier.WIN);
		matchOddWin.setOdd(1.0);

		MatchOdds matchOddDraw = new MatchOdds();
		matchOddDraw.setSpecifier(Specifier.DRAW);
		matchOddWin.setOdd(1.0);

		MatchOdds matchOddLose = new MatchOdds();
		matchOddLose.setSpecifier(Specifier.LOSE);
		matchOddLose.setOdd(1.0);

		List<MatchOdds> matchOdds = List.of(matchOddWin, matchOddDraw, matchOddLose);
		request.setOdds(matchOdds);

		return request;
	}

	private Match createMatch(Long id, String owner, Sport sport, LocalDate date, LocalTime time) {
		Match m = new Match();
		m.setId(id);
		m.setOwner(owner);
		m.setSport(sport);
		m.setMatchDate(date);
		m.setMatchTime(time);
		return m;
	}
}
