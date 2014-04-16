package au.edu.alveo.client.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
* Created by amack on 17/03/14.
*/
public class JSONLDKeys {
	public static final String ANNOTATIONS = fullAlveoUri("annotations");
	public static final String COMMON_PROPERTIES = "http://purl.org/dada/schema/0.2#commonProperties";
	public static final String TEXT_ANNOTATION_VALUE_DEFAULT_TYPE = "http://purl.org/dada/schema/0.2#TextAnnotation";
	public static final Set<String> TEXT_ANNOTATION_VALUE_TYPE_SYNS = new HashSet<String>();
	public static final String PURL_SCHEMA = fullAttribUri("");

	public static final String ANNOTATION_END = fullAttribUri("end");
	public static final String ANNOTATION_START = fullAttribUri("start");
	public static final String ANNOTATION_LABEL = fullAttribUri("label");
	public static final String ANNOTATION_TYPE = fullAttribUri("type");
	public static final String ANNOTATION_ANNOTATES = fullAlveoUri("annotates");

	public static final String ITEM_METADATA = fullAlveoUri("metadata");
	public static final String ITEM_DOCUMENTS = fullAlveoUri("documents");
	public static final String ITEM_ANNOTATIONS_URL = fullAlveoUri("annotations_url");
	public static final String ITEM_PRIMARY_TEXT_URL = fullAlveoUri("primary_text_url");

	public static final String DOCUMENT_TYPE = "http://purl.org/dc/terms/type";
	public static final String DOCUMENT_URL = fullAlveoUri("url");
	public static final String DOCUMENT_SIZE = fullAlveoUri("size");

	private static final String alveoSchemaBase = "http://alveo.edu.au/schema/";
	private static String fullAlveoUri(String suffix) {
		return String.format("%s%s", alveoSchemaBase, suffix);
	}


	private static final String attribBase = "http://purl.org/dada/schema/0.2";
	private static String fullAttribUri(String suffix) {
		return String.format("%s#%s", attribBase, suffix);
	}


	static {
		String[] tavSyns = {
				TEXT_ANNOTATION_VALUE_DEFAULT_TYPE,
				"http://purl.org/dada/schema/0.2#UTF8Region"
		};
		TEXT_ANNOTATION_VALUE_TYPE_SYNS.addAll(Arrays.asList(tavSyns));
	}
}
