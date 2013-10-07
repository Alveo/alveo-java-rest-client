package com.nicta.hls.vlabclient;

import java.util.List;
import java.util.Map;

import com.nicta.hls.vlabclient.RestClient.Document;

/** A representation of an Item from a item list in the HCS vLab REST API
 * 
 * @author andrew.mackinlay
 *
 */
public interface VLabAnnotation {

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

}