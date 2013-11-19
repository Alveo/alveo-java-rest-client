package com.nicta.vlabclient.entity;

import com.nicta.vlabclient.RestClientException;

public class EntityNotFoundException extends RestClientException {

	public EntityNotFoundException() {
	}

	public EntityNotFoundException(String message) {
		super(message);
	}

	public EntityNotFoundException(Throwable cause) {
		super(cause);
	}

	public EntityNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
