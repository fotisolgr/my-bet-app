package com.mybetapp.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Specifier {
	WIN, DRAW, LOSE;

	@JsonCreator
	public static Specifier fromString(String value) {
		for (Specifier specifier : Specifier.values()) {
			if (specifier.name().equalsIgnoreCase(value)) {
				return specifier;
			}
		}

		throw new IllegalArgumentException("Invalid specifier value: " + value + ". Allowed values are: "
				+ Arrays.stream(Specifier.values()).map(Enum::name).collect(Collectors.joining(" or ")) + ".");
	}
}
