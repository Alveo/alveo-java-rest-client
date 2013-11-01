package com.nicta.vlabclient.entity;

import java.util.List;
import java.util.Map;

/** A representation of an Item from a item list in the HCS vLab REST API
 * 
 * @author andrew.mackinlay
 *
 */
public interface Item {

	/** Return the documents associated with this item */
	public List<Document> documents();

	/** Return the URI (a valid REST URI) from which this item was retrieved */
	public String getUri();

	/** Get the URL at which the primary text of the item is stored;
	 * 
	 * Library users will probably be more interested in the {@link #primaryText()} method
	 * which does the text retrieval automatically.
	 * @return a URL storing the primary text
	 */
	public String getPrimaryTextUrl();

	/** Get the primary text of the item 
	 * 
	 * @return the primary text associated with an item
	 */
	public String primaryText();

	/** Get the metadata associated with an item
	 * 
	 * @return a mapping from all metadata key names to metadata values
	 */
	public Map<String, String> getMetadata();
	
	/** Get the (possibly empty) list of associated annotations.
	 * 
	 * @return
	 * @throws UnsupportedLDSchemaException if the schema cannot be interpreted, meaning it has
	 *  a stucture which this code version cannot map to a POJO
	 */
	public List<Annotation> getAnnotations() throws UnsupportedLDSchemaException;

	/** Get the (possibly empty) list of associated text annotations.
	 * 
	 * @return
	 * @throws UnsupportedLDSchemaException if the schema cannot be interpreted, meaning it has
	 *  a stucture which this code version cannot map to a POJO
	 */
	public List<TextAnnotation> getTextAnnotations() throws UnsupportedLDSchemaException;

	
	/** Get the (possibly empty) list of association annotations, returning the 
	 * JSONLD representation of each annotation. In this representation, the
	 * keys are full URIs.
	 * 
	 */
	public List<Map<String, Object>> annotationsAsJSONLD();


}