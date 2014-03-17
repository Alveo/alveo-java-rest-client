package com.nicta.vlabclient;

/**
 * Created by amack on 19/02/14.
 */
public class InvalidAnnotationException extends RestClientException {
	public InvalidAnnotationException() {
		super();
	}

	public InvalidAnnotationException(String message) {
		super(message);
	}

	public InvalidAnnotationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidAnnotationException(Throwable cause) {
		super(cause);

	}
}
