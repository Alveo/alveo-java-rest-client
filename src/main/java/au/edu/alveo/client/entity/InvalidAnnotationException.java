package au.edu.alveo.client.entity;

/**
 * Created by amack on 19/02/14.
 */
public class InvalidAnnotationException extends HCSvLabException {
	public InvalidAnnotationException() {
		super();
	}

	public InvalidAnnotationException(String message) {
		super(message);
	}

	public InvalidAnnotationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidAnnotationException(Throwable cause) {
		super(cause);

	}
}
