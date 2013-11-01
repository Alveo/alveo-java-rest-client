package com.nicta.vlabclient.entity;

/** A representation of a 'document' (version of an item) in the HCSvLab API with associated metadata
 * 
 * @author andrew.mackinlay
 *
 */
public interface Document extends RawDocument {


	/** Get the 'type' of the document
	 * 
	 * @return The document type according to vLab, indicating how the document relates to the item,
	 *  such as "Original", "Raw" or "Text"
	 */
	public String getType();

	/** Get the document size
	 * 
	 * @return A string representation of the document size, such as "1.8kB"
	 */
	public String getSize();


}