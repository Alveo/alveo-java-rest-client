package com.nicta.vlabclient.entity;

import com.nicta.vlabclient.RestClientException;

/**
 * Created by amack on 18/02/14.
 */
public class UploadIntegrityException extends RestClientException {
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
