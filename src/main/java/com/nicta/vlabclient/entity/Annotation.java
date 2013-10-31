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
	public float getStart();

	/** Return the end offset of the annotation
	 */
	public float getEnd();
	
	public static class JSONLDKeys {
		public static final String ANNOTATION = "http://purl.org/dada/schema/0.2/annotations";
		public static final String COMMON_PROPERTIES = "http://purl.org/dada/schema/0.2/commonProperties";
	}

}