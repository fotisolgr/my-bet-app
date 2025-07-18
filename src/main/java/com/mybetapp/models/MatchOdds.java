package com.mybetapp.models;

import com.mybetapp.enums.Specifier;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MatchOdds {

	@NotNull(message = "Specifier is required")
	private Specifier specifier;

	@NotNull(message = "Odd is required")
	private Double odd;
}
