package com.mybetapp.models;

import com.mybetapp.enums.Sport;
import com.mybetapp.validations.DifferentTeamsDto;
import com.mybetapp.validations.ValidMatchOdds;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@DifferentTeamsDto
@ValidMatchOdds
public class SaveMatchRequest {

	private String description;

	@NotBlank(message = "Match date is required")
	@Schema(type = "string", example = "2025-08-22", format = "date")
	private String matchDate;

	@NotBlank(message = "Match time is required")
	@Schema(type = "string", example = "20:45:00", format = "time")
	private String matchTime;

	@NotBlank(message = "Team A is required")
	private String teamA;

	@NotBlank(message = "Team B is required")
	private String teamB;

	@NotNull(message = "Sport is required")
	private Sport sport;

	@NotNull(message = "Odds list cannot be null")
	@NotEmpty(message = "Odds list cannot be empty")
	private List<@Valid MatchOdds> odds; // also validates nested objects
}
