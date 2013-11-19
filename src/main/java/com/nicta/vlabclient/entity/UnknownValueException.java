package com.nicta.vlabclient.entity;

import com.nicta.vlabclient.RestClientException;

public class UnknownValueException extends RestClientException {

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
