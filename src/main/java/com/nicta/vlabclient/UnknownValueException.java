package com.nicta.vlabclient;

public class UnknownValueException extends RestJsonDataException {

	public UnknownValueException() {
	}

	public UnknownValueException(String message) {
		super(message);
	}

	public UnknownValueException(Throwable cause) {
		super(cause);
	}

	public UnknownValueException(String message, Throwable cause) {
		super(message, cause);
	}

}
