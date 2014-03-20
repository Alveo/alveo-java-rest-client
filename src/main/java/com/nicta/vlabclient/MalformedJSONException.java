package com.nicta.vlabclient;

public class MalformedJSONException extends HCSvLabRuntimeException {

	public MalformedJSONException() {
	}

	public MalformedJSONException(String message) {
		super(message);
	}

	public MalformedJSONException(Throwable cause) {
		super(cause);
	}

	public MalformedJSONException(String message, Throwable cause) {
		super(message, cause);
	}

}
