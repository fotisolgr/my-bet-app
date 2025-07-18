package com.mybetapp.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DifferentTeamsDtoValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DifferentTeamsDto {

	String message() default "Team A and Team B must be different";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
