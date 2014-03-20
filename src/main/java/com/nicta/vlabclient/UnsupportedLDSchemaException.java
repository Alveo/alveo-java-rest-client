package com.nicta.vlabclient;

/** Thrown when the schema has a type we do not recognize, or we don't see an
 * the expected type for a particular key so mapping
 * to a POJO is not possible.
 *
 * @author andrew.mackinlay
 *
 */
public class UnsupportedLDSchemaException extends RestJsonDataException {
	public UnsupportedLDSchemaException(String msg) {
		super(msg);
	}

	public UnsupportedLDSchemaException(Throwable cause) {
		super(cause);
	}

	public UnsupportedLDSchemaException(String message, Throwable cause) {
		super(message, cause);
	}
}
