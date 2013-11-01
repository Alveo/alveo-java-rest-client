package com.nicta.vlabclient.entity;

/** A representation of an Item from a item list in the HCS vLab REST API
 * 
 * @author andrew.mackinlay
 *
 */
public interface Annotation {

	/** Return the type of the annotation (such as "laughter", "speaker") according to HCS vLab */
	public String getType();

	/** Return the label assigned to the annotation
	 */
	public String getLabel();

	/** Return the start offset of the annotation
	 */
	public double getStart();

	/** Return the end offset of the annotation
	 */
	public double getEnd();
	
	public RawDocument getAnnotationTarget();
	
	public static class JSONLDKeys {
		public static final String ANNOTATION = "http://purl.org/dada/schema/0.2/annotations";
		public static final String COMMON_PROPERTIES = "http://purl.org/dada/schema/0.2/commonProperties";
		public static final String TEXT_ANNOTATION_TYPE = "http://purl.org/dada/schema/0.2/TextAnnotation";
		private static final String attribBase = "http://purl.org/dada/schema/0.2";
		
		private static String fullUri(String suffix) {
			return String.format("%s/%s", attribBase, suffix);
		}
		public static final String END_ATTRIB = fullUri("end");
		public static final String START_ATTRIB = fullUri("start");
		public static final String LABEL_ATTRIB = fullUri("label");
		public static final String TYPE_ATTRIB = fullUri("type");
		public static final String ANNOTATES_ATTRIB = fullUri("annotates");
	}

}