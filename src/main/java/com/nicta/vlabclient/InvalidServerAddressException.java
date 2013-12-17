package com.nicta.vlabclient;

public class InvalidServerAddressException extends RestClientException {

	public InvalidServerAddressException() {
	}

	public InvalidServerAddressException(String message) {
		super(message);
	}

	public InvalidServerAddressException(Throwable cause) {
		super(cause);
	}

	public InvalidServerAddressException(String message, Throwable cause) {
		super(message, cause);
	}

}
