package au.edu.alveo.client.entity;

public class InvalidServerAddressException extends HCSvLabException {

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
