package com.nicta.vlabclient;

public class UnauthorizedAPIKeyException extends RestClientException {

	public UnauthorizedAPIKeyException() {
	}

	public UnauthorizedAPIKeyException(String message) {
		super(message);
	}

	public UnauthorizedAPIKeyException(Throwable cause) {
		super(cause);
	}

	public UnauthorizedAPIKeyException(String message, Throwable cause) {
		super(message, cause);
	}

}
