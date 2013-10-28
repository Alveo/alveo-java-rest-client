package com.nicta.vlabclient;

/** A representation of a 'document' (version of an item) in the HCSvLab API
 * 
 * @author andrew.mackinlay
 *
 */
public interface Document {

	/** Get the URL where the raw text is stored. 
	 * 
	 * The {@link #rawText()} method is probably more useful to end users
	 * 
	 * @return The URL from which the raw text can be retrieved
	 */
	public String getRawTextUrl();

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

	/** Get the raw document text 
	 * 
	 * @return The raw text of the document.
	 */
	public String rawText();

}