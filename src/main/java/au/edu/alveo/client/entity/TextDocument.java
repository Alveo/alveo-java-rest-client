package au.edu.alveo.client.entity;

/** A representation of a 'document' (version of an item) in the HCSvLab API 
 * without any attached metadata (size, type)
 * 
 * @see Document
 * 
 * @author andrew.mackinlay
 *
 */

public interface TextDocument extends Document {

	/** Get the raw document text 
	 * 
	 * @return The raw text of the document.
	 */
	public String rawText();

}
