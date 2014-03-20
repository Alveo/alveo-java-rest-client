package com.nicta.vlabclient.entity;

/**
 * Created by amack on 18/02/14.
 */
public class UploadIntegrityException extends HCSvLabException {
	public UploadIntegrityException() {
		super();
	}

	public UploadIntegrityException(String message) {
		super(message);
	}

	public UploadIntegrityException(Throwable cause) {
		super(cause);
	}

	public UploadIntegrityException(String message, Throwable cause) {
		super(message, cause);
	}
}
