package com.nicta.vlabclient.entity;

import com.nicta.vlabclient.RestClientException;

/** Thrown when the schema has a type we do not recognize, so mapping
 * to a POJO is not possible.
 * 
 * @author andrew.mackinlay
 *
 */
public class UnsupportedLDSchemaException extends RestClientException {
	public UnsupportedLDSchemaException(String msg) {
		super(msg);
	}
	
	public UnsupportedLDSchemaException(Throwable cause) {
		super(cause);
	}
}
