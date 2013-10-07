package com.nicta.hls.vlabclient;

import java.util.List;
import java.util.Map;

import com.nicta.hls.vlabclient.RestClient.Document;

/** A representation of an Item from a item list in the HCS vLab REST API
 * 
 * @author andrew.mackinlay
 *
 */
public interface VLabItem {

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
	
	/** Get the (possibly empty) list of association annotations.
	 * 
	 *  This is simply a shortcut for users who do not care about the
	 *  extra metadata attached to the annotations (which will often be true),
	 *  which is equivalent to calling {@link VLabAnnotationGroup#getAnnotations()} on
	 *  the result of {@link #getAnnotationGroup()}*/
	public List<VLabAnnotation> getAnnotations();
	
	/** Return an AnnotationGroup object, from which metadata and annotations
	 * can be retrieved
	 * @return
	 */
	public VLabAnnotationGroup getAnnotationGroup();

}