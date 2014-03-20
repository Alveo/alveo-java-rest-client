package com.nicta.vlabclient;

/**
 * Created by amack on 19/02/14.
 */
public class InvalidServerResponseException extends HCSvLabRuntimeException {
	public InvalidServerResponseException() {
		super();
	}

	public InvalidServerResponseException(String s) {
		super(s);
	}

	public InvalidServerResponseException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public InvalidServerResponseException(Throwable throwable) {
		super(throwable);
	}
}
