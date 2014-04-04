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
	
	/** Get the text documents which are linked to this item */
	public List<TextDocument> textDocuments();
	
	/** Get the audio documents which are linked to this item */
	public List<AudioDocument> audioDocuments();

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
	 * @return The annotations which are linked to this item.
	 */
	public List<Annotation> getAnnotations();

	/** Get the (possibly empty) list of associated text annotations.
	 * 
	 * @return The text annotations which are linked to this item.
	 */
	public List<TextAnnotation> getTextAnnotations();

	/** Get the (possibly empty) list of associated text annotations.
	 * 
	 * @return The text annotations which are linked to this item.
	 */
	public List<AudioAnnotation> getAudioAnnotations();

	
	
	/** Get the (possibly empty) list of associated annotations, returning the
	 * JSONLD representation of each annotation. In this representation, the
	 * keys are full URIs.
	 * 
	 */
	public List<Map<String, Object>> annotationsAsJSONLD();


	/** Store a new set of annotations associated with this item to the server
	 *
	 * @param annotations a list of annotation objects
	 * @throws EntityNotFoundException If the item could not be found
	 * @throws UploadIntegrityException If the upload fails due to a problem with the data
	 * @throws InvalidAnnotationException If the annotation is invalid for any reason
	 */
	public void storeNewAnnotations(List<? extends Annotation> annotations) throws EntityNotFoundException,
			UploadIntegrityException, InvalidAnnotationException;
}