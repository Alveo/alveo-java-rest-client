package au.edu.alveo.client.entity;

public class AlveoException extends Exception {

	public AlveoException() {
	}

	public AlveoException(String message) {
		super(message);
	}

	public AlveoException(Throwable cause) {
		super(cause);
	}

	public AlveoException(String message, Throwable cause) {
		super(message, cause);
	}

}
