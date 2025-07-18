package com.mybetapp.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Sport {
	FOOTBALL, BASKETBALL;

	@JsonCreator
	public static Sport fromString(String value) {
		for (Sport sport : Sport.values()) {
			if (sport.name().equalsIgnoreCase(value)) {
				return sport;
			}
		}

		throw new IllegalArgumentException("Invalid sport value: " + value + ". Allowed values are: "
				+ Arrays.stream(Sport.values()).map(Enum::name).collect(Collectors.joining(" or ")) + ".");
	}
}
