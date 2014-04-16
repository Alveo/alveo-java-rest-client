package au.edu.alveo.client.entity;

public class UnauthorizedAPIKeyException extends AlveoException {

	public UnauthorizedAPIKeyException() {
	}

	public UnauthorizedAPIKeyException(String message) {
		super(message);
	}

	public UnauthorizedAPIKeyException(Throwable cause) {
		super(cause);
	}

	public UnauthorizedAPIKeyException(String message, Throwable cause) {
		super(message, cause);
	}

}
