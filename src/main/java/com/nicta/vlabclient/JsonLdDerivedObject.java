package com.nicta.vlabclient;

import com.nicta.vlabclient.entity.UnsupportedLDSchemaException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by amack on 17/03/14.
 */
public class JsonLdDerivedObject {
	protected final Map<String, Object> ldValues = new LinkedHashMap<String, Object>();

	protected Object getValue(String key) throws UnsupportedLDSchemaException {
		return getValue(key, false);
	}

	protected Object getValue(String key, boolean optional) throws UnsupportedLDSchemaException {
		Object res = ldValues.get(key);
		if (res == null && !optional)
			throw new UnsupportedLDSchemaException(String.format(
					"No key '%s' found for annotation", key));
		return res;
	}
}
