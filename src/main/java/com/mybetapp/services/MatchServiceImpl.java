package com.mybetapp.services;

import com.mybetapp.dto.MatchDTO;
import com.mybetapp.entities.Match;
import com.mybetapp.entities.MatchOdds;
import com.mybetapp.enums.Sanitization;
import com.mybetapp.enums.Sport;
import com.mybetapp.models.SaveMatchRequest;
import com.mybetapp.repositories.MatchRepository;
import com.mybetapp.util.Result;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchServiceImpl implements MatchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MatchServiceImpl.class);

	private final MatchRepository matchRepository;

	public MatchServiceImpl(MatchRepository matchRepository) {
		this.matchRepository = matchRepository;
	}

	@Override
	public Result<Page<MatchDTO>> getPaginatedMatches(int page, int size, String sortBy, String direction, String owner,
			String sport, LocalDate matchDate) {
		LOGGER.info(
				"Fetching paginated matches with filters - page: {}, size: {}, sortBy: {}, direction: {}, owner: {}, sport: {}, matchDate: {}",
				page, size, sortBy, direction, owner, sport, matchDate);

		try {
			Sort sort = getSort(sortBy, direction);
			Pageable pageable = PageRequest.of(page, size, sort);

			Specification<Match> spec = buildMatchSpecification(sanitizeUserInput(owner, Sanitization.LOWERCASE), sport,
					matchDate);
			Page<Match> pagedMatches = matchRepository.findAll(spec, pageable);
			Page<MatchDTO> dtoPage = pagedMatches.map(this::getMatchDTO);

			return Result.ok(dtoPage);
		} catch (Exception e) {
			LOGGER.error("Error fetching paginated matches", e);
			return Result.error("Failed to fetch paginated matches: " + e.getMessage());
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<MatchDTO> saveMatch(SaveMatchRequest saveMatchRequest) {
		LOGGER.info("Trying to save match: {}", saveMatchRequest);

		LocalDate matchDate = parseDate(saveMatchRequest.getMatchDate());
		LocalTime matchTime = parseTime(saveMatchRequest.getMatchTime());
		String teamA = sanitizeUserInput(saveMatchRequest.getTeamA(), Sanitization.UPPERCASE);
		String teamB = sanitizeUserInput(saveMatchRequest.getTeamB(), Sanitization.UPPERCASE);
		Sport sport = saveMatchRequest.getSport();

		String currentUser = getCurrentUsername();
		if (currentUser == null) {
			return Result.error("Unauthorized: no user found in context");
		}

		boolean matchAlreadyExists = matchRepository.existsByTeamAAndTeamBAndMatchDateAndMatchTimeAndSport(teamA, teamB,
				matchDate, matchTime, sport);

		if (matchAlreadyExists) {
			LOGGER.warn("Match: {}, already exists. Aborting persistence.", saveMatchRequest);
			return Result.error(sport + " match between " + teamA + " and " + teamB + " at " + matchDate + " "
					+ matchTime + " already exists");
		}

		Match match = new Match();
		mapRequestToMatch(saveMatchRequest, match);
		matchRepository.save(match);

		LOGGER.info("Match: {}, successfully saved", match);
		return Result.ok(getMatchDTO(match));
	}

	@Transactional(rollbackFor = Exception.class)
	public Result<MatchDTO> updateMatch(Long matchId, SaveMatchRequest updateRequest) {
		LOGGER.info("Updating match with ID {}: {}", matchId, updateRequest);

		Optional<Match> optionalMatch = matchRepository.findById(matchId);
		if (optionalMatch.isEmpty()) {
			return Result.error("Match with ID " + matchId + " not found");
		}

		Match match = optionalMatch.get();

		String currentUser = getCurrentUsername();
		if (currentUser == null) {
			return Result.error("Unauthorized: no user found in context");
		}

		boolean isCurrentUserEqualToMatchOwner = isCurrentUserEqualToMatchOwner(currentUser, match);
		if (!isCurrentUserEqualToMatchOwner) {
			return Result.error("You do not have permission to update this match");
		}

		mapRequestToMatch(updateRequest, match);

		matchRepository.save(match);

		LOGGER.info("Match with ID {} updated successfully", matchId);
		return Result.ok(getMatchDTO(match));
	}

	@Override
	@Transactional
	public Result<Void> deleteMatch(Long matchId) {
		try {
			LOGGER.info("Attempting to delete match with id: {}", matchId);

			Optional<Match> matchOpt = matchRepository.findById(matchId);

			if (matchOpt.isEmpty()) {
				LOGGER.warn("Match with id {} not found", matchId);
				return Result.error("NOT_FOUND: Match with id " + matchId + " does not exist.");
			}

			String currentUser = getCurrentUsername();
			if (currentUser == null) {
				return Result.error("Unauthorized: no user found in context");
			}

			Match match = matchOpt.get();

			boolean isCurrentUserEqualToMatchOwner = isCurrentUserEqualToMatchOwner(currentUser, match);
			if (!isCurrentUserEqualToMatchOwner) {
				return Result.error("You do not have permission to update this match");
			}

			matchRepository.delete(matchOpt.get());

			LOGGER.info("Deleted match with id: {}", matchId);

			return Result.okVoid();
		} catch (Exception e) {
			LOGGER.error("Error while deleting match with id {}: {}", matchId, e.getMessage(), e);
			return Result.error("INTERNAL_ERROR: Failed to delete match: " + e.getMessage());
		}
	}

	private MatchDTO getMatchDTO(Match match) {
		MatchDTO matchDTO = new MatchDTO();

		matchDTO.setMatchId(match.getId());
		matchDTO.setMatchOwner(match.getOwner());
		matchDTO.setDescription(match.getDescription());
		matchDTO.setMatchDate(match.getMatchDate());
		matchDTO.setMatchTime(match.getMatchTime());
		matchDTO.setTeamA(match.getTeamA());
		matchDTO.setTeamB(match.getTeamB());
		matchDTO.setSport(match.getSport());

		List<com.mybetapp.models.MatchOdds> oddsDtoList = match.getOdds().stream().map(entityOdd -> {
			com.mybetapp.models.MatchOdds dtoOdd = new com.mybetapp.models.MatchOdds();
			dtoOdd.setSpecifier(entityOdd.getSpecifier());
			dtoOdd.setOdd(entityOdd.getOdd());
			return dtoOdd;
		}).collect(Collectors.toList());

		matchDTO.setOdds(oddsDtoList);

		return matchDTO;
	}

	private void mapRequestToMatch(SaveMatchRequest request, Match match) {
		match.setOwner(getCurrentUsername());
		match.setDescription(sanitizeUserInput(request.getDescription(), Sanitization.UPPERCASE));
		match.setMatchDate(parseDate(request.getMatchDate()));
		match.setMatchTime(parseTime(request.getMatchTime()));
		match.setTeamA(sanitizeUserInput(request.getTeamA(), Sanitization.UPPERCASE));
		match.setTeamB(sanitizeUserInput(request.getTeamB(), Sanitization.UPPERCASE));
		match.setSport(request.getSport());

		// Clear odds before setting new ones to avoid duplicates on update
		match.getOdds().clear();

		request.getOdds().forEach(dto -> {
			MatchOdds odd = new MatchOdds();
			odd.setSpecifier(dto.getSpecifier());
			odd.setOdd(dto.getOdd());
			odd.setMatch(match);
			match.getOdds().add(odd);
		});
	}

	private LocalDate parseDate(String dateStr) {
		return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}

	private LocalTime parseTime(String timeStr) {
		return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
	}

	private String getCurrentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
			return jwt.getClaimAsString("preferred_username"); // or "sub"
		}
		return null;
	}

	private String sanitizeUserInput(String input, Sanitization sanitization) {
		if (input == null) {
			return null;
		}

		if (Sanitization.LOWERCASE.equals(sanitization)) {
			return input.trim().toLowerCase();
		}

		return input.trim().toUpperCase();
	}

	private String sanitize(String input) {
		if (input == null) {
			return null;
		}
		return input.trim().toUpperCase();
	}

	private boolean isCurrentUserEqualToMatchOwner(String currentUser, Match match) {
		boolean isOwner = currentUser.equals(match.getOwner());

		if (!isOwner) {
			LOGGER.warn("User {} attempted to update match {} owned by {}", currentUser, match.getId(),
					match.getOwner());
		}

		return isOwner;
	}

	private Sort getSort(String sortBy, String direction) {
		boolean isDesc = "desc".equalsIgnoreCase(direction);

		return switch (sortBy) {
			case "owner" -> {
				LOGGER.info("Sorting by 'owner' in {} order", direction);
				yield Sort.by(isDesc ? Sort.Order.desc("owner") : Sort.Order.asc("owner"),
						isDesc ? Sort.Order.desc("matchDate") : Sort.Order.asc("matchDate"));
			}

			case "sport" -> {
				LOGGER.info("Sorting by 'sport' in {} order", direction);
				yield Sort.by(isDesc ? Sort.Order.desc("sport") : Sort.Order.asc("sport"),
						isDesc ? Sort.Order.desc("matchDate") : Sort.Order.asc("matchDate"));
			}

			case "matchDate" -> {
				LOGGER.info("Sorting by 'matchDate' and 'matchTime' in {} order", direction);
				yield Sort.by(isDesc
						? List.of(Sort.Order.desc("matchDate"), Sort.Order.desc("matchTime"))
						: List.of(Sort.Order.asc("matchDate"), Sort.Order.asc("matchTime")));
			}

			default -> {
				LOGGER.warn("Unknown sortBy value '{}', defaulting to 'matchDate' descending", sortBy);
				yield Sort.by(Sort.Order.desc("matchDate"));
			}
		};
	}

	private Specification<Match> buildMatchSpecification(String owner, String sport, LocalDate matchDate) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (owner != null && !owner.isEmpty()) {
				predicates.add(cb.equal(root.get("owner"), owner));
			}
			if (sport != null && !sport.isEmpty()) {
				predicates.add(cb.equal(root.get("sport"), sport));
			}
			if (matchDate != null) {
				predicates.add(cb.equal(root.get("matchDate"), matchDate));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
