package com.nicta.vlabclient;

public class RestClientException extends Exception {

	public RestClientException() {
	}

	public RestClientException(String message) {
		super(message);
	}

	public RestClientException(Throwable cause) {
		super(cause);
	}

	public RestClientException(String message, Throwable cause) {
		super(message, cause);
	}

}
