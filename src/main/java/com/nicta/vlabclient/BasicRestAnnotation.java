package com.nicta.vlabclient;

import com.nicta.vlabclient.entity.Annotation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by amack on 20/02/14.
 */
public abstract class BasicRestAnnotation implements Annotation {
	protected final Map<String, Object> ldValues = new LinkedHashMap<String, Object>();
	protected String annId;
	protected String type;
	protected String label;
	protected String valueType;
	protected double start;
	protected double end;

	/** Internal use only - for subclasses which set the values differently */
	BasicRestAnnotation() {
	}

	public BasicRestAnnotation(String type, String label, double start, double end, String valueType) {
		this.type = type;
		this.label = label;
		this.start = start;
		this.end = end;
		this.valueType = valueType;
		this.annId = null;
		initJSONValuesFromFields();
	}

	protected void initJSONValuesFromFields() {
		if (annId != null)
			ldValues.put("@id", annId);
		ldValues.put(JSONLDKeys.TYPE_ATTRIB, type);
		if (label != null)
			ldValues.put(JSONLDKeys.LABEL_ATTRIB, label);
		ldValues.put("@type", valueType);
		initCustomJSONValuesFromFields();
	}

	protected void initCustomJSONValuesFromFields() {
		ldValues.put(JSONLDKeys.START_ATTRIB, start);
		ldValues.put(JSONLDKeys.END_ATTRIB, end);
	}

	public String getType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public double getStart() {
		return start;
	}

	public double getEnd() {
		return end;
	}

	@Override
	public String getValueType() {
		return valueType;
	}

	public String getId() {
		return annId;
	}

	@Override
	public Map<String, Object> uriToValueMap() {
		return ldValues;
	}
}
