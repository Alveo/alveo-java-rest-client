package com.nicta.vlabclient;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.jsonldjava.core.JSONLD;
import com.github.jsonldjava.core.JSONLDProcessingError;
import com.github.jsonldjava.utils.JSONUtils;
import com.nicta.vlabclient.JsonApi.JsonCatalogItem;
import com.nicta.vlabclient.JsonApi.JsonDocument;
import com.nicta.vlabclient.JsonApi.JsonItemList;
import com.nicta.vlabclient.JsonApi.VersionResult;
import com.nicta.vlabclient.entity.*;
import com.nicta.vlabclient.entity.Annotation.JSONLDKeys;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The primary class to use to interact with the HCS vLab REST API - use this to
 * retrieve items
 * 
 * @author Andrew MacKinlay
 * 
 */
public class RestClient {
	private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

	private final String serverBaseUri;
	private final String apiKey;
	private final Client client;

	private static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>();

	/**
	 * Construct a new REST client instance. Since this creates an associated {@link javax.ws.rs.client.Client}
	 * instance, which "may be a rather expensive operation", and in addition there is overhead of
	 * checking the validity of the API key, callers are advised to keep usage of this constructor to a minimum.
	 * 
	 * @param serverBaseUri
	 *            The base URI of the server to construct API calls to the JSON
	 *            REST API.
	 * @param apiKey
	 *            The API key for the relevant user account, available from the
	 *            HCS vLab web interface
	 * @throws UnknownServerAPIVersionException
	 *             If the server's reported API version is incompatible with
	 *             this code
	 * @throws InvalidServerAddressException
	 *             If it is not possible to connect to the server, probably
	 *             because of an invalid address
	 */
	public RestClient(String serverBaseUri, String apiKey) throws UnknownServerAPIVersionException,
			InvalidServerAddressException {
		this.serverBaseUri = serverBaseUri;
		this.apiKey = apiKey;
		// use this HTTP client which respects JVM proxy settings
		// (this doesn't do any harm, and helps in testing with Betamax)
		ClientHttpEngine httpEngine = new ApacheHttpClient4Engine(newHttpClient());
//		// instead of the more usual ClientBuilder.newClient(), we do this to specify the engine:
		client = new ResteasyClientBuilder().httpEngine(httpEngine).build();
		checkVersion();
	}
	
	private HttpClient newHttpClient() {
		HttpParams params = new BasicHttpParams();
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000); // 60 sec timeout since ann retrieval can be slow
		return new SystemDefaultHttpClient(params);
	}

	private void checkVersion() throws InvalidServerAddressException,
			UnknownServerAPIVersionException {
		VersionResult verRes;
		try {
			verRes = getJsonInvocBuilder(serverBaseUri + "/version").get(VersionResult.class);
		} catch (ProcessingException e) {
			throw new InvalidServerAddressException("Server URI " + serverBaseUri
					+ " may be invalid", e);
		}
		String version = verRes.apiVersion;
		if (!version.equals("2.0") && !version.startsWith("Sprint_")
				&& !version.startsWith("HEAD ("))
			throw new UnknownServerAPIVersionException(
					"This codebase is not designed for API version " + version);
	}

	/**
	 * A representation of an HCS vLab item list which uses the vLab REST client
	 * to retrieve related data
	 * 
	 * @author andrew.mackinlay
	 * 
	 */
	private class ItemListImpl implements ItemList {
		private final JsonItemList fromJson;
		private final String uri;

		private ItemListImpl(JsonItemList raw, String uri) {
			fromJson = raw;
			this.uri = uri;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.ItemList#getUri()
		 */
		public String getUri() {
			return uri;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.ItemList#itemUris()
		 */
		public String[] itemUris() {
			return fromJson.getItems();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.ItemList#name()
		 */
		public String name() {
			return fromJson.getName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.ItemList#numItems()
		 */
		public int numItems() {
			return fromJson.getNumItems();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.ItemList#getCatalogItems()
		 */
		public List<Item> getCatalogItems() throws UnauthorizedAPIKeyException {
			List<Item> cis = new ArrayList<Item>();
			for (String itemUri : itemUris()) 
				cis.add(getItemByUri(itemUri));
			return cis;
		}
	}

	/**
	 * A representation of an Item from a item list in the HCS vLab REST API
	 * 
	 * @author andrew.mackinlay
	 * 
	 */
	private class ItemImpl implements Item {
		private final JsonCatalogItem fromJson;
		private final String uri;

		private List<Map<String, Object>> cachedJsonLdAnns = null;

		private ItemImpl(JsonCatalogItem raw, String uri) {
			fromJson = raw;
			this.uri = uri;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.Item#documents()
		 */
		public List<Document> documents() {
			JsonDocument[] jsonDocs = fromJson.getDocuments();
			List<Document> docs = new ArrayList<Document>(jsonDocs.length);
			for (JsonDocument jd : jsonDocs) {
				Document doc;
				if (jd.getType().equals("Audio"))
					doc = new AudioDocumentImpl(jd);
				else
					doc = new TextDocumentImpl(jd);
				docs.add(doc);
			}
			return docs;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.Item#getUri()
		 */
		public String getUri() {
			return uri;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.Item#getPrimaryTextUrl()
		 */
		public String getPrimaryTextUrl() {
			return fromJson.getPrimaryTextUrl();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.Item#primaryText()
		 */
		public String primaryText() {
			return getTextInvocBuilder(getPrimaryTextUrl()).get(String.class);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.Item#getMetadata()
		 */
		public Map<String, String> getMetadata() {
			return fromJson.getMetadata();
		}

		public List<Annotation> getAnnotations() throws UnsupportedLDSchemaException {
			List<Annotation> anns = new ArrayList<Annotation>();
			Map<String, Document> rawDocCache = Collections.synchronizedMap(new HashMap<String, Document>(1));
			for (Map<String, Object> jsonLdAnn : annotationsAsJSONLD()) {
				Annotation ann;
				String valueType = (String) jsonLdAnn.get("@type");
				if (valueType.equals(JSONLDKeys.TEXT_ANNOTATION_VALUE_TYPE))
					ann = new TextAnnotationImpl(jsonLdAnn, rawDocCache);
				else
					ann = new AudioAnnotationImpl(jsonLdAnn, rawDocCache);
				// if (ann == null)
				// throw new UnsupportedLDSchemaException(String.format(
				// "Unknown annotation type %s", annClass));
				anns.add(ann);
			}
			return anns;
		}

		public List<TextAnnotation> getTextAnnotations() throws UnsupportedLDSchemaException {
			List<TextAnnotation> res = new ArrayList<TextAnnotation>();
			for (Annotation ann : getAnnotations()) {
				try {
					res.add((TextAnnotation) ann);
				} catch (ClassCastException ignored) {
				}
			}
			return res;
		}

		@Override
		public List<AudioAnnotation> getAudioAnnotations() throws UnsupportedLDSchemaException {
			List<AudioAnnotation> res = new ArrayList<AudioAnnotation>();
			for (Annotation ann : getAnnotations()) {
				try {
					res.add((AudioAnnotation) ann);
				} catch (ClassCastException ignored) {
				}
			}
			return res;
		}

		@SuppressWarnings("unchecked")
		public List<Map<String, Object>> annotationsAsJSONLD() {
			// memoize since this is often slow, and also since clients might
			// underlyingly call it mutple times for different types of annotations.
			if (cachedJsonLdAnns != null)
				return cachedJsonLdAnns;
			cachedJsonLdAnns = new ArrayList<Map<String, Object>>();
			Object jsonObj = getJsonObject();
			Map<String, Object> compacted = getResolvedVersion(jsonObj);
			Map<String, Object> annWrapper = jmap(compacted.get(JSONLDKeys.ANNOTATIONS));
			Map<String, Object> commonProps = jmap(compacted.get(JSONLDKeys.COMMON_PROPERTIES));
			List<Object> annObjects = (List<Object>) annWrapper.get("@list");
			for (Object annObj : annObjects) {
				Map<String, Object> annAsMap = jmap(annObj);
				annAsMap.putAll(commonProps);
				cachedJsonLdAnns.add(annAsMap);
			}
			return cachedJsonLdAnns;
		}

		@SuppressWarnings("unchecked")
		private Map<String, Object> getResolvedVersion(Object jsonObj) {
			// use 'compact' here to resolve each annotation against the
			// context,
			// rather than to shorten each annotation (which is its primary use)
			try {
				return (Map<String, Object>) JSONLD.compact(jsonObj, EMPTY_MAP);
			} catch (JSONLDProcessingError e) {
				throw new MalformedJSONException(e);
			}
		}

		@SuppressWarnings("unchecked")
		// helper purely to avoid verbose casts
		private Map<String, Object> jmap(Object jsonObject) {
			return (Map<String, Object>) jsonObject;
		}

		private Object getJsonObject() {
			try {
				return JSONUtils.fromString(getRawJson());
			} catch (JsonParseException e) {
				throw new MalformedJSONException(e);
			} catch (JsonMappingException e) {
				throw new MalformedJSONException(e);
			}
		}

		private String getRawJson() {
			String url = fromJson.getAnnotationsUrl();
			return getJsonInvocBuilder(url).get(String.class);
		}

		public List<TextDocument> textDocuments() {
			List<TextDocument> docs = new ArrayList<TextDocument>();
			for (Document doc : documents()) {
				try {
					docs.add((TextDocument) doc);
				} catch (ClassCastException ignored) {
				}
			}
			return docs;
		}

		public List<AudioDocument> audioDocuments() {
			List<AudioDocument> docs = new ArrayList<AudioDocument>();
			for (Document doc : documents()) {
				try {
					docs.add((AudioDocument) doc);
				} catch (ClassCastException ignored) {
				}
			}
			return docs;
		}

	}

	/**
	 * A class representing a 'document' (version of an item) in the HCSvLab API
	 * 
	 * @author andrew.mackinlay
	 * 
	 */
	private class DocumentImpl implements Document {
		private String docType = null;
		private String docSize = null;
		private final String docUrl;

		private DocumentImpl(JsonDocument raw) {
			docType = raw.getType();
			docSize = raw.getSize();
			docUrl = raw.getUrl();
			if (docType == null || docUrl == null)
				throw new MalformedJSONException(String.format(
						"An expected value was missing from %s", raw));
		}

		private DocumentImpl(String url) {
			docUrl = url;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.Document#getType()
		 */
		public String getType() throws UnknownValueException {
			if (docType == null)
				throw new UnknownValueException(
						"Document type not explicitly set; this may be because the document"
								+ " comes from an annotation");
			return docType;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.Document#getSize()
		 */
		@Nullable
		public String getSize() throws UnknownValueException {
			return docSize;
		}

		public String getDataUrl() {
			return docUrl;
		}

	}

	private class TextDocumentImpl extends DocumentImpl implements TextDocument {

		private TextDocumentImpl(String url) {
			super(url);
		}

		private TextDocumentImpl(JsonDocument fromJson) {
			super(fromJson);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.Document#rawText()
		 */
		public String rawText() {
			return getTextInvocBuilder(getDataUrl()).get(String.class);
		}
	}

	public class AudioDocumentImpl extends DocumentImpl implements AudioDocument {

		private AudioDocumentImpl(String url) {
			super(url);
		}

		private AudioDocumentImpl(JsonDocument fromJson) {
			super(fromJson);
		}

		public byte[] getData() {
			return getDataInvocBuilder(getDataUrl()).get(byte[].class); // XXX:
																		// not
																		// tested
		}

	}

	private abstract class AnnotationImpl implements Annotation {

		private final Map<String, Object> ldValues;
		private final Map<String, Document> rawDocCache;

		private String annId;
		private String type;
		private String label;
		private String valueType;
		private double start;
		private double end;

		private AnnotationImpl(Map<String, Object> raw, Map<String, Document> docCache)
				throws UnsupportedLDSchemaException {
			ldValues = raw;
			rawDocCache = docCache;
			initFieldsFromJSONValues();
		}

		private AnnotationImpl(String type, String label, double start, double end, String valueType) {
			this.type = type;
			this.label = label;
			this.start = start;
			this.end = end;
			this.valueType = valueType;
			ldValues = new LinkedHashMap<String, Object>();
			initJSONValuesFromFields();
			rawDocCache = null;
		}

		private Map<String, Object> mapForJson() {
			return ldValues;
		}

		private Object getValue(String key) throws UnsupportedLDSchemaException {
			return getValue(key, false);
		}

		private Object getValue(String key, boolean optional) throws UnsupportedLDSchemaException {
			Object res = ldValues.get(key);
			if (res == null && !optional)
				throw new UnsupportedLDSchemaException(String.format(
						"No key '%s' found for annotation", key));
			return res;
		}

		private void initFieldsFromJSONValues() throws UnsupportedLDSchemaException {
			annId = (String) getValue("@id");
			type = (String) getValue(JSONLDKeys.TYPE_ATTRIB);
			label = (String) getValue(JSONLDKeys.LABEL_ATTRIB, true);
			start = readDouble(getValue(JSONLDKeys.START_ATTRIB));
			end = readDouble(getValue(JSONLDKeys.END_ATTRIB));
			valueType = (String) getValue("@type");
			getValue(JSONLDKeys.ANNOTATES_ATTRIB); // check for key only
		}

		private double readDouble(Object value) {
			try {
				return (Double) value;
			} catch (ClassCastException e) {
				return Double.parseDouble((String) value);
			}
		}

		private void initJSONValuesFromFields() {
			if (annId != null)
				ldValues.put("@id", annId);
			ldValues.put(JSONLDKeys.TYPE_ATTRIB, type);
			ldValues.put(JSONLDKeys.LABEL_ATTRIB, label);
			ldValues.put(JSONLDKeys.START_ATTRIB, start);
			ldValues.put(JSONLDKeys.END_ATTRIB, end);
			ldValues.put("@type", valueType);
		}

		public String getType() {
			return type;
		}

		public String getLabel() {
			return label;
		}

		public double getStart() {
			return start;
		}

		public double getEnd() {
			return end;
		}

		@Override
		public String getValueType() {
			return valueType;
		}

		public Document getAnnotationTarget() {
			// memoize, since clients might call it multiple times
			String url = annTargetUrl();
			Document annTgt = rawDocCache.get(url);
			if (annTgt == null) {
				annTgt = getNewAnnotationTarget();
				rawDocCache.put(url, annTgt);
			}
			return annTgt;
		}

		protected abstract Document getNewAnnotationTarget();

		protected String annTargetUrl() {
			return (String) ldValues.get(JSONLDKeys.ANNOTATES_ATTRIB);
		}

		public String getId() {
			return annId;
		}

		public String toString() {
			return String.format("<%s>%s(%s)@%1.1f,%1.1f->%s", getId(), getType(), getLabel(),
					getStart(), getEnd(), getAnnotationTarget().getDataUrl());
		}

	}

	private class TextAnnotationImpl extends AnnotationImpl implements TextAnnotation {
		private final int startOffset;
		private final int endOffset;

		private TextAnnotationImpl(Map<String, Object> raw, Map<String, Document> docCache)
				throws UnsupportedLDSchemaException {
			super(raw, docCache);
			startOffset = (int) getStart();
			endOffset = (int) getEnd();
			if (Math.abs(startOffset - getStart()) + Math.abs(endOffset - getEnd()) > 0.0)
				throw new UnsupportedLDSchemaException(String.format(
						"Invalid non-integer text offsets %0.2f, %0.2f", getStart(), getEnd()));
		}

		public int getStartOffset() {
			return (int) getStart();
		}

		public int getEndOffset() {
			return (int) getEnd();
		}

		public String toString() {
			return String.format("<%s>%s(%s)@%d,%d->%s", getId(), getType(), getLabel(),
					getStartOffset(), getEndOffset(), getAnnotationTarget().getDataUrl());
		}

		public TextDocument getTextAnnotationTarget() {
			return (TextDocument) getAnnotationTarget();
		}

		@Override
		protected Document getNewAnnotationTarget() {
			return new TextDocumentImpl(annTargetUrl());
		}

	}

	private class AudioAnnotationImpl extends AnnotationImpl implements AudioAnnotation {

		private AudioAnnotationImpl(Map<String, Object> raw, Map<String, Document> docCache)
				throws UnsupportedLDSchemaException {
			super(raw, docCache);
		}

		public String toString() {
			return String.format("<%s>%s(%s)@%1.1f,%1.1f->%s", getId(), getType(), getLabel(),
					getStart(), getEnd(), getAnnotationTarget().getDataUrl());
		}

		public AudioDocument getAudioAnnotationTarget() {
			return (AudioDocument) getAnnotationTarget();
		}

		@Override
		protected Document getNewAnnotationTarget() {
			return new AudioDocumentImpl(annTargetUrl());
		}

	}

	/**
	 * Return the URI of the item list given an ID. For end-users,
	 * {@link RestClient#getItemList(String)} is probably more useful
	 * 
	 * @param itemListId
	 *            the ID of the item list
	 * @return a URI stored as a string
	 */
	public String getItemListUri(String itemListId) {
		return String.format("%s/item_lists/%s", serverBaseUri, itemListId);
	}

	/**
	 * Get the item list with the supplied ID
	 * 
	 * @param itemListId
	 *            the ID of the item list
	 * @return the item list object with the given ID
	 * @throws EntityNotFoundException
	 *             If the item list cannot be found
	 * @throws UnauthorizedAPIKeyException
	 *             if the API key does not permit access
	 */
	public ItemList getItemList(String itemListId) throws EntityNotFoundException,
			UnauthorizedAPIKeyException {
		try {
			return getItemListFromUri(getItemListUri(itemListId));
		} catch (NotFoundException e) {
			throw new EntityNotFoundException("Could not find entity with ID " + itemListId);
		}
	}

	/**
	 * Get the catalog item with the supplied ID
	 * 
	 * @param itemId
	 *            the ID of the item, such as 'hcsvlab:456'
	 * @return the requested item
	 * @throws EntityNotFoundException
	 *             If the item with the supplied ID could not be found 
	 * @throws UnauthorizedAPIKeyException
	 *             if the API key does not permit access
	 */
	public Item getItem(String itemId) throws EntityNotFoundException, UnauthorizedAPIKeyException {
		String itemUri = serverBaseUri + "catalog/" + itemId;
		try {
			return getItemByUri(itemUri);
		} catch (NotFoundException e) {
			throw new EntityNotFoundException("Could not find item with URI " + itemUri);
		}
	}
	
	/**
	 * Get the catalog item at the supplied URI
	 * 
	 * @param itemUri
	 *            the fully qualified URI of the item, such as 'http://vlab.example.org/catalog/hcsvlab:456'
	 * @return the requested item
	 * @throws UnauthorizedAPIKeyException
	 *             if the API key does not permit access
	 */
	public Item getItemByUri(String itemUri) throws UnauthorizedAPIKeyException {
		try {
			return new ItemImpl(getJsonInvocBuilder(itemUri).get(JsonCatalogItem.class), itemUri);
		} catch (NotAuthorizedException e) {
			throw new UnauthorizedAPIKeyException("Provided API key " + apiKey
					+ " was not accepted by the server");
		}
	}

	
	/**
	 * Get the item list from the supplied rest URI
	 * 
	 * @param itemListUri
	 *            the fully qualified URI for the REST API
	 * @return the item list object with the given ID
	 * @throws EntityNotFoundException
	 *             If the item list cannot be found
	 * @throws UnauthorizedAPIKeyException
	 *             if the API key does not permit access
	 * @see #getItemList(String)
	 */
	public ItemList getItemListFromUri(String itemListUri) throws UnauthorizedAPIKeyException, EntityNotFoundException {
		try {
			JsonItemList itemListJson = getJsonInvocBuilder(itemListUri).get(JsonItemList.class);
			return new ItemListImpl(itemListJson, itemListUri);
		} catch (NotAuthorizedException e) {
			throw new UnauthorizedAPIKeyException("Provided API key " + apiKey
					+ " was not accepted by the server");
		} catch (NotFoundException e) {
			throw new EntityNotFoundException("Could not find entity with URI " + itemListUri);
		}
	}

	/**
	 * Get the raw JSON for the item list from the supplied ID. Mostly useful
	 * for debugging
	 * 
	 * @param itemListId
	 *            the ID of the item list
	 * @return unformatted JSON from the HCS vLab REST server
	 * @throws EntityNotFoundException
	 *             If the item list cannot be found
	 * @throws UnauthorizedAPIKeyException
	 *             if the API key does not permit access
	 * @see #getItemList(String)
	 */
	public String getItemListJson(String itemListId) throws EntityNotFoundException,
			UnauthorizedAPIKeyException {
		try {
			return getItemListJsonFromUri(getItemListUri(itemListId));
		} catch (NotFoundException e) {
			throw new EntityNotFoundException("Could not find entity with ID " + itemListId);
		}
	}

	/**
	 * Get the raw JSON for the item list from the supplied URI. Mostly useful
	 * for debugging
	 * 
	 * @param itemListUri
	 *            the fully qualified URI for the REST API
	 * @return unformatted JSON from the HCS vLab REST server
	 * @throws UnauthorizedAPIKeyException
	 *             if the API key does not permit access
	 * @see RestClient#getItemListJson(String)
	 */
	public String getItemListJsonFromUri(String itemListUri) throws UnauthorizedAPIKeyException {
		try {
			return getJsonInvocBuilder(itemListUri).get(String.class);
		} catch (NotAuthorizedException e) {
			throw new UnauthorizedAPIKeyException("Provided API key " + apiKey
					+ " was not accepted by the server");
		}
	}

	private Invocation.Builder getJsonInvocBuilder(String uri) {
		return getInvocBuilder(uri, MediaType.APPLICATION_JSON);
	}

	private Invocation.Builder getTextInvocBuilder(String uri) {
		return getInvocBuilder(uri, MediaType.TEXT_PLAIN);
	}

	private Invocation.Builder getDataInvocBuilder(String uri) {
		return getInvocBuilder(uri, MediaType.APPLICATION_OCTET_STREAM);
	}

	private Invocation.Builder getInvocBuilder(String uri, String contType) {
		return client.target(uri).request(contType).accept(contType).header("X-API-KEY", apiKey);
	}
}
