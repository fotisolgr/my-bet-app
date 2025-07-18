package com.mybetapp.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MatchOddsConstraintValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMatchOdds {

	String message() default "Odds list must contain at most one WIN, one DRAW, and one LOSE, and have maximum size of 3";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
