package au.edu.alveo.client;

/**
 * Created by amack on 19/02/14.
 */
public class AlveoRuntimeException extends RuntimeException {
	public AlveoRuntimeException() {
		super();
	}

	public AlveoRuntimeException(String s) {
		super(s);
	}

	public AlveoRuntimeException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public AlveoRuntimeException(Throwable throwable) {
		super(throwable);
	}
}
