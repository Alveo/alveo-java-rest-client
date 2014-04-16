package au.edu.alveo.client;

/**
 * Created by amack on 19/02/14.
 */
public class HCSvLabRuntimeException extends RuntimeException {
	public HCSvLabRuntimeException() {
		super();
	}

	public HCSvLabRuntimeException(String s) {
		super(s);
	}

	public HCSvLabRuntimeException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public HCSvLabRuntimeException(Throwable throwable) {
		super(throwable);
	}
}
