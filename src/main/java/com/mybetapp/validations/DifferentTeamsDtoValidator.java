package com.mybetapp.validations;

import com.mybetapp.models.SaveMatchRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DifferentTeamsDtoValidator implements ConstraintValidator<DifferentTeamsDto, SaveMatchRequest> {

	@Override
	public boolean isValid(SaveMatchRequest value, ConstraintValidatorContext context) {
		if (value == null) {
			return true; // skip validation if null (can use @NotNull separately)
		}
		if (value.getTeamA() == null || value.getTeamB() == null) {
			return true; // let @NotBlank handle this
		}

		boolean valid = !value.getTeamA().equalsIgnoreCase(value.getTeamB());
		if (!valid) {
			// Customize violation message on the class level
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
					.addPropertyNode("teamB") // you can attach error to a specific field
					.addConstraintViolation();
		}
		return valid;
	}
}
