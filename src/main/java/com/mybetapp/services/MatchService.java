package com.mybetapp.services;

import com.mybetapp.dto.MatchDTO;
import com.mybetapp.models.SaveMatchRequest;
import com.mybetapp.util.Result;
import org.springframework.data.domain.Page;

public interface MatchService {
	Result<Page<MatchDTO>> getPaginatedMatches(int page, int size, String sortBy, String direction);

	Result<MatchDTO> saveMatch(SaveMatchRequest saveMatchRequest);

	Result<MatchDTO> updateMatch(Long matchId, SaveMatchRequest saveMatchRequest);

	Result<Void> deleteMatch(Long matchId);
}
