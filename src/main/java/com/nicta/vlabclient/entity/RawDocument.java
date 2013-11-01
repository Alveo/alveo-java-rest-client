package com.nicta.vlabclient.entity;

/** A representation of a 'document' (version of an item) in the HCSvLab API 
 * without any attached metadata (size, type)
 * 
 * @see Document
 * 
 * @author andrew.mackinlay
 *
 */

public interface RawDocument {

	/** Get the URL where the raw text is stored. 
	 * 
	 * The {@link #rawText()} method is probably more useful to end users
	 * 
	 * @return The URL from which the raw text can be retrieved
	 */
	public String getRawTextUrl();

	/** Get the raw document text 
	 * 
	 * @return The raw text of the document.
	 */
	public String rawText();

}
