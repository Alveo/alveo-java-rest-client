package com.nicta.vlabclient.entity;

/** A representation of a 'document' (version of an item) in the HCSvLab API with associated metadata
 * 
 * @author andrew.mackinlay
 *
 */
public interface Document {
	/** Get the URL where the data is stored. 
	 * 
	 * The {@link #rawText()} method is probably more useful to end users
	 * 
	 * @return The URL from which the raw text can be retrieved
	 */
	public String getDataUrl();


	/** Get the 'type' of the document
	 * 
	 * @return The document type according to vLab, indicating how the document relates to the item,
	 *  such as "Original", "Raw" or "Text"
	 */
	public String getType() throws UnknownValueException;

	/** Get the document size
	 * 
	 * @return A string representation of the document size, such as "1.8kB"
	 */
	public String getSize() throws UnknownValueException;


}