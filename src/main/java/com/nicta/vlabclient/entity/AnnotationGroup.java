package com.nicta.vlabclient.entity;

import java.util.List;

/** A representation of an Item from a item list in the HCS vLab REST API
 * 
 * @author andrew.mackinlay
 *
 */
public interface AnnotationGroup {

	/** Return the URI from which the annotation group was retrieved */
	public String getUri();
	
	/** Return the associated Item ID */
	public String getItemId();

	/** Return the URL of the associate utterance
	 */
	public String getUtteranceUrl();

	/** Return the number of associated annotations
	 */
	public int getNumAnnotations();

	/** Return annotations from this group (likely to be the method you want)
	 */
	public List<Annotation> getAnnotations();

}