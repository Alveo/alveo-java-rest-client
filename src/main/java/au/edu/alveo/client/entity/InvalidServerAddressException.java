package au.edu.alveo.client.entity;

public class InvalidServerAddressException extends AlveoException {

	public InvalidServerAddressException() {
	}

	public InvalidServerAddressException(String message) {
		super(message);
	}

	public InvalidServerAddressException(Throwable cause) {
		super(cause);
	}

	public InvalidServerAddressException(String message, Throwable cause) {
		super(message, cause);
	}

}
