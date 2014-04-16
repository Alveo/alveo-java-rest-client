package au.edu.alveo.client.entity;

import java.util.Map;

/** A representation of an Item from a item list in the Alveo REST API
 * 
 * @author andrew.mackinlay
 *
 */
public interface Annotation {
	
	/** Return the unique ID associated with the annotation */
	public String getId();

	/** Return the type of the annotation (such as "laughter", "speaker") according to Alveo */
	public String getType();

	/** Return the label assigned to the annotation
	 */
	public String getLabel();

	/** Return the start offset of the annotation
	 */
	public double getStart();

	/** Return the end offset of the annotation
	 */
	public double getEnd();

	/** Return the <a href="http://www.w3.org/TR/json-ld/#typed-values">JSON-LD value type</a>
	 */
	public String getValueType();

	/** Return a mapping containing URIs as keys corresponding to fields, and their matching values
	 * This is used for converting to and from JSON
	 *
	 * The URIs correspond to JSON-LD URIs and therefore also to RDF predicate URIs on the server side
	 *
	 * @return a URI to value mapping
	 */
	public Map<String, Object> uriToValueMap();

	public Document getAnnotationTarget();


}