package au.edu.alveo.client;

import au.edu.alveo.client.entity.Document;
import au.edu.alveo.client.entity.JSONLDKeys;
import au.edu.alveo.client.entity.TextAnnotation;
import au.edu.alveo.client.entity.TextDocument;

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
