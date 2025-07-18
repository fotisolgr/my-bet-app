package com.mybetapp.validations;

import com.mybetapp.models.MatchOdds;
import com.mybetapp.models.SaveMatchRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MatchOddsConstraintValidator implements ConstraintValidator<ValidMatchOdds, SaveMatchRequest> {

	@Override
	public boolean isValid(SaveMatchRequest value, ConstraintValidatorContext context) {
		if (value == null || value.getOdds() == null) {
			return true; // Let other validations handle null cases
		}

		List<MatchOdds> odds = value.getOdds();

		// Must be exactly 3 odds
		if (odds.size() != 3) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Odds list must contain exactly 3 items")
					.addPropertyNode("error").addConstraintViolation();
			return false;
		}

		// Count specifiers, case-insensitive
		Map<String, Long> specifierCounts = odds.stream().map(matchOdd -> matchOdd.getSpecifier().name().toUpperCase())
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		// Must contain WIN, DRAW, and LOSE exactly once
		List<String> requiredSpecifiers = List.of("WIN", "DRAW", "LOSE");

		for (String specifier : requiredSpecifiers) {
			Long count = specifierCounts.get(specifier);
			if (count == null || count != 1) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("Must have exactly one '" + specifier + "' specifier")
						.addPropertyNode("error").addConstraintViolation();
				return false;
			}
		}

		// No extra specifiers allowed (since size is 3 and these 3 are mandatory, this
		// is implicit)
		return true;
	}
}
