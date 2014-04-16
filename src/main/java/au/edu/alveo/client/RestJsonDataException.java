package au.edu.alveo.client;

/**
 * Created by amack on 17/03/14.
 */
public class RestJsonDataException extends HCSvLabRuntimeException {
	public RestJsonDataException() {
	}

	public RestJsonDataException(String message) {
		super(message);
	}

	public RestJsonDataException(Throwable cause) {
		super(cause);
	}

	public RestJsonDataException(String message, Throwable cause) {
		super(message, cause);
	}
}
