package com.nicta.vlabclient;

import com.nicta.vlabclient.entity.Document;
import com.nicta.vlabclient.entity.JSONLDKeys;
import com.nicta.vlabclient.entity.TextAnnotation;
import com.nicta.vlabclient.entity.TextDocument;

/**
 * Created by amack on 20/02/14.
 */
public class TextRestAnnotation extends BasicRestAnnotation implements TextAnnotation {

	private Document annotationTarget = null;

	public TextRestAnnotation(String type, String label, double start, double end, String valueType) {
		super(type, label, start, end, valueType);
	}

	public TextRestAnnotation(String type, String label, double start, double end) {
		this(type, label, start, end, JSONLDKeys.TEXT_ANNOTATION_VALUE_DEFAULT_TYPE);
	}


	@Override
	public int getStartOffset() {
		return (int) getStart();
	}

	@Override
	public int getEndOffset() {
		return (int) getEnd();
	}

	@Override
	public TextDocument getTextAnnotationTarget() {
		return (TextDocument) getAnnotationTarget();
	}

	public void setAnnotationTarget(Document doc) {
		annotationTarget = doc;
	}

	@Override
	public Document getAnnotationTarget() {
		return annotationTarget;
	}

	@Override
	protected void initCustomJSONValuesFromFields() {
		ldValues.put(JSONLDKeys.ANNOTATION_START, getStartOffset());
		ldValues.put(JSONLDKeys.ANNOTATION_END, getEndOffset());
	}

}
