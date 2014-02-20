package com.nicta.vlabclient;

/**
 * Created by amack on 19/02/14.
 */
public class RestClientRuntimeException extends RuntimeException {
	public RestClientRuntimeException() {
		super();
	}

	public RestClientRuntimeException(String s) {
		super(s);
	}

	public RestClientRuntimeException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public RestClientRuntimeException(Throwable throwable) {
		super(throwable);
	}
}
