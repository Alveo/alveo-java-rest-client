package au.edu.alveo.client;

public class UnknownServerAPIVersionException extends AlveoRuntimeException {

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
