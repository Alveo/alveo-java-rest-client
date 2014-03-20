package com.nicta.vlabclient;

import com.nicta.vlabclient.entity.HCSvLabException;

public class UnknownServerAPIVersionException extends HCSvLabRuntimeException {

	public UnknownServerAPIVersionException() {
	}

	public UnknownServerAPIVersionException(String message) {
		super(message);
	}

	public UnknownServerAPIVersionException(Throwable cause) {
		super(cause);
	}

	public UnknownServerAPIVersionException(String message, Throwable cause) {
		super(message, cause);
	}

}
