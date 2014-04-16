package au.edu.alveo.client.entity;

public interface TextAnnotation extends Annotation {
	public int getStartOffset();
	
	public int getEndOffset();
	
	public TextDocument getTextAnnotationTarget();
}
