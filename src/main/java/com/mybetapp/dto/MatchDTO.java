package com.mybetapp.dto;

import com.mybetapp.enums.Sport;
import com.mybetapp.models.MatchOdds;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MatchDTO {

	private Long matchId;
	private String matchOwner;
	private String description;
	private LocalDate matchDate;
	private LocalTime matchTime;
	private String teamA;
	private String teamB;
	private Sport sport;
	private List<MatchOdds> odds;
}
