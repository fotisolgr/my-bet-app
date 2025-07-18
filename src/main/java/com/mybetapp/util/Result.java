package com.mybetapp.util;

import lombok.Getter;

@Getter
public class Result<T> {
	private final T value;
	private final String error;

	private Result(T value, String error) {
		this.value = value;
		this.error = error;
	}

	public static <T> Result<T> ok(T value) {
		return new Result<>(value, null);
	}

	public static <T> Result<T> error(String errorMessage) {
		return new Result<>(null, errorMessage);
	}

	public static Result<Void> okVoid() {
		return new Result<>(null, null);
	}

	public boolean isSuccess() {
		return error == null;
	}
}
