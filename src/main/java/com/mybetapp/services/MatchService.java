package com.mybetapp.services;

import com.mybetapp.dto.MatchDTO;
import com.mybetapp.models.SaveMatchRequest;
import com.mybetapp.util.Result;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public interface MatchService {
	Result<Page<MatchDTO>> getPaginatedMatches(int page, int size, String sortBy, String direction, String owner,
			String sport, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate matchDate);

	Result<MatchDTO> saveMatch(SaveMatchRequest saveMatchRequest);

	Result<MatchDTO> updateMatch(Long matchId, SaveMatchRequest saveMatchRequest);

	Result<Void> deleteMatch(Long matchId);
}
