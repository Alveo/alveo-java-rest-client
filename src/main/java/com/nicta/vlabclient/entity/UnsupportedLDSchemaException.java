package com.nicta.vlabclient.entity;

/** Thrown when the schema has a type we do not recognize, so mapping
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
}
