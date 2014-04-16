package au.edu.alveo.client;

public class MalformedJSONException extends AlveoRuntimeException {

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
