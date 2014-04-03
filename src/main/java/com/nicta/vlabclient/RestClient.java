package com.nicta.vlabclient;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JSONUtils;
import com.nicta.vlabclient.JsonApi.JsonItemList;
import com.nicta.vlabclient.JsonApi.VersionResult;
import com.nicta.vlabclient.entity.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	private final HttpClient httpClient;

	private static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>();
	private Map<String,Object> cachedJSONLDSchema = null;

	private static final Charset CHARSET = Charset.forName("UTF-8");
	private static final MessageDigest HASHER;

	static {
		try {
			HASHER = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}


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
		httpClient = newHttpClient();
		// use this HTTP client which respects JVM proxy settings
		// (this doesn't do any harm, and helps in testing with Betamax)
		ClientHttpEngine httpEngine = new ApacheHttpClient4Engine(httpClient);
//		// instead of the more usual ClientBuilder.newClient(), we do this to specify the engine:
		client = new ResteasyClientBuilder().httpEngine(httpEngine).build();
		checkVersion();
	}

	/**
	 * Get a SPARQL repository for the given collection, which can be
	 * used to execute arbitrary SPARQL queries against the server.
	 *
	 * Example usage:
	 * <pre>
	 {@code SPARQLRepository repo = client.getSparqlRepository(collection);
	 String sparql = "SELECT DISTINCT ?type WHERE { ?ann <http://purl.org/dada/schema/0.2#type> ?type . }";
	 RepositoryConnection conn = repo.getConnection();
	 TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
	 TupleQueryResult result = query.evaluate();
	 result.getBindingNames();
	 while (result.hasNext()) {
	 	BindingSet bs = result.next();
	 	System.out.println("Type: " + bs.getValue("type"));
	 }
	 }
	 * </pre>
	 *

	 *
	 * @param collectionName The name of the collection to query against
	 * @return a SPARQL repository which can be used for querying.
	 *
	 * @throws RepositoryException If there is an error with the repository
	 */
	public SPARQLRepository getSparqlRepository(String collectionName) throws RepositoryException {
		SPARQLRepository repo = new SPARQLRepository(String.format("%ssparql/%s", serverBaseUri, collectionName));
		repo.initialize();
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("X-API-KEY", apiKey);
		repo.setAdditionalHttpHeaders(headers);
		return repo;
	}
	
	private HttpClient newHttpClient() {
		HttpClientBuilder builder = HttpClientBuilder.create().useSystemProperties(); // respect system proxy settings
		RequestConfig config = RequestConfig.custom().setConnectTimeout(20000).build(); // in case we get slow annotation retrieval
		builder.setDefaultRequestConfig(config);
		return builder.build();
	}

	private void checkVersion() throws InvalidServerAddressException,
			UnknownServerAPIVersionException {
		VersionResult verRes;
		try {
			verRes = getJsonInvocBuilder(serverBaseUri + "/version.json").get(VersionResult.class);
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
	private class ItemImpl extends JsonLdDerivedObject implements Item {
		private final String uri;

		private List<Map<String, Object>> cachedJsonLdAnns = null;

		private Map<String, String> metadata;
		private String primaryTextUrl;
		private String annotationsUrl;
		private List<Document> documents;

		private ItemImpl(Map<String, Object> raw, String uri) throws UnsupportedLDSchemaException, UnknownValueException {
			ldValues.clear();
			ldValues.putAll(raw);
			this.uri = uri;
			initFieldsFromJSONValues();
		}


		@SuppressWarnings("unchecked")
		private void initFieldsFromJSONValues() throws UnsupportedLDSchemaException, UnknownValueException {
			try {
				metadata = (Map<String, String>) getValue(JSONLDKeys.ITEM_METADATA);
				primaryTextUrl = (String) getValue(JSONLDKeys.ITEM_PRIMARY_TEXT_URL);
				annotationsUrl = (String) getValue(JSONLDKeys.ITEM_ANNOTATIONS_URL);
				List<Map<String, Object>> rawDocs = (List<Map<String, Object>>) getValue(JSONLDKeys.ITEM_DOCUMENTS);
				initializeDocuments(rawDocs);
			} catch (ClassCastException e) {
				throw new UnsupportedLDSchemaException("Error converting JSON-LD to Java - type mismatch", e);
			}
		}

		private void initializeDocuments(List<Map<String, Object>> rawDocs) throws UnsupportedLDSchemaException, UnknownValueException {
			documents = new ArrayList<Document>(rawDocs.size());
			for (Map<String, Object> rd : rawDocs) {
				Document doc = new DocumentImpl(rd);
				documents.add(getDocInstanceForType(doc));
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.Item#documents()
		 */
		public List<Document> documents() {
			return documents;
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
			return primaryTextUrl;
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
			return metadata;
		}

		/**
		 *
		 * @throws UnsupportedLDSchemaException if the schema cannot be interpreted, meaning it has
		 *  a stucture which this code version cannot map to a POJO
		 */
		@Override
		public List<Annotation> getAnnotations() throws UnsupportedLDSchemaException {
			List<Annotation> anns = new ArrayList<Annotation>();
			Map<String, Document> rawDocCache = Collections.synchronizedMap(new HashMap<String, Document>(1));
			for (Map<String, Object> jsonLdAnn : annotationsAsJSONLD()) {
				Annotation ann;
				String valueType = (String) jsonLdAnn.get("@type");
				if (JSONLDKeys.TEXT_ANNOTATION_VALUE_TYPE_SYNS.contains(valueType))
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

		/**
		 *
		 * @throws UnsupportedLDSchemaException if the schema cannot be interpreted, meaning it has
		 *  a stucture which this code version cannot map to a POJO
		 */
		@Override
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
		/**
		 *
		 * @throws UnsupportedLDSchemaException if the schema cannot be interpreted, meaning it has
		 *  a stucture which this code version cannot map to a POJO
		 */
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
			Object jsonObj = getJsonObject(annotationsUrl);
			workAroundValueTypeIssue(jmap(jsonObj));
			Map<String, Object> compacted = getResolvedVersion(jsonObj);
			Map<String, Object> commonProps = jmap(compacted.get(JSONLDKeys.COMMON_PROPERTIES));
			List<Object> annObjects = (List<Object>) compacted.get(JSONLDKeys.ANNOTATIONS);
			for (Object annObj : annObjects) {
				Map<String, Object> annAsMap = jmap(annObj);
				annAsMap.putAll(commonProps);
				cachedJsonLdAnns.add(annAsMap);
			}
			return cachedJsonLdAnns;
		}

		private void workAroundValueTypeIssue(Map<String, Object> jsonObj) {
			// XXX: workaround for HCSVLAB-812
			List<Object> anns = (List<Object>) jsonObj.get("hcsvlab:annotations");
			for (Object annJson: anns) {
				Map<String, Object> ann = jmap(annJson);
				String valType = (String) ann.get("@type");
				if (valType != null && !valType.startsWith("#") && !valType.contains(":")) // no namespace prefix or frag prefix?
					ann.put("@type", JSONLDKeys.PURL_SCHEMA + valType); // insert explicit prefix
				if (valType == null) // XXX: workaround for HCSVLAB-719 - uploaded annotations can get null @type
					ann.put("@type", JSONLDKeys.TEXT_ANNOTATION_VALUE_DEFAULT_TYPE); // at least JSONLD won't choke
			}
		}

		@Override
		public void storeNewAnnotations(List<Annotation> annotations)
				throws EntityNotFoundException, UploadIntegrityException, InvalidAnnotationException {
			String annUploadUri = uri + "/annotations";
			// HCS vLab expects a multipart file upload of the JSON rather than raw post data.
			// so we have to use the Apache HTTP client directly to build up the call
			// instead of using JAX-RS.
			String uploadableJson = JSONUtils.toString(jsonMapForAnnUpload(annotations));
			MultipartEntityBuilder mpeBuilder = MultipartEntityBuilder.create();
			mpeBuilder.setCharset(CHARSET);
			byte[] uploadableBytes = uploadableJson.getBytes(CHARSET);
			mpeBuilder.addBinaryBody("file", uploadableBytes, ContentType.APPLICATION_JSON,
					HASHER.digest(uploadableBytes).toString() + ".json"); // hash contents to give unique filenames
			HttpPost httpPost = new HttpPost(annUploadUri);
			httpPost.addHeader("X-API-KEY", apiKey);
			httpPost.setEntity(mpeBuilder.build());
			HttpResponse response;
			try {
				response = httpClient.execute(httpPost);
			} catch (IOException e) {
				throw new InvalidServerResponseException(e);
			}
			LOG.info("Using URI {}, about to post entity {}", annUploadUri,
					uploadableJson);
			Map<String, Object> responseMap;
			try {
				responseMap = (Map<String, Object>) JSONUtils.fromInputStream(response.getEntity().getContent());
			} catch (JsonProcessingException e) {
				throw new InvalidServerResponseException(e);
			} catch (IOException e) {
				throw new InvalidServerResponseException(e);
			}
			String error = (String) responseMap.get("error");
			if (error != null) {
				if (error.startsWith("No item with ID"))
					throw new EntityNotFoundException(error);
				else
					throw new UploadIntegrityException(error);
			}
		}

		private Map<String, Object> jsonMapForAnnUpload(List<Annotation> annotations) throws InvalidAnnotationException {
			LinkedHashMap<String, Object> jsonMap = new LinkedHashMap<String, Object>(1);
			List<Map<String, Object>> annsJson = new ArrayList<Map<String, Object>>(annotations.size());
			for (Annotation ann: annotations)
				annsJson.add(ann.uriToValueMap());
			jsonMap.put("@graph", annsJson);
			LOG.info("Raw annotation JSON: {}", jsonMap);
			Map<String, Object> compacted;
			try {
				compacted = JsonLdProcessor.compact(jsonMap, defaultJSONLDSchema(), new JsonLdOptions());
			} catch (JsonLdError e) {
				throw new InvalidAnnotationException("Error compacting annotations using JSONLD", e);
			}
			return compacted;
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

	private String schemaURL() {
		return serverBaseUri + "/schema/json-ld";
	}

	private Map<String, Object> defaultJSONLDSchema() {
		if (cachedJSONLDSchema != null)
			return cachedJSONLDSchema;
		String jsonSchemaString = getJsonInvocBuilder(schemaURL()).get(String.class);
		try {
			cachedJSONLDSchema = (Map<String, Object>) JSONUtils.fromString(jsonSchemaString);
		} catch (JsonProcessingException e) {
			LOG.warn("Invalid JSON schema found at {}; using empty context for uploaded annotations " +
					"(this should not affect functionality but will increase upload time)", schemaURL());
			cachedJSONLDSchema = new LinkedHashMap<String, Object>(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return cachedJSONLDSchema;
	}

	/**
	 * A class representing a 'document' (version of an item) in the HCSvLab API
	 * 
	 * @author andrew.mackinlay
	 * 
	 */
	private class DocumentImpl extends JsonLdDerivedObject implements Document {
		private String docType = null;
		private String docSize = null;
		private String docUrl;

		private DocumentImpl(Map<String, Object> raw) throws UnsupportedLDSchemaException {
			ldValues.clear();
			ldValues.putAll(raw);
			initFieldsFromJSONValues();
			if (docType == null || docUrl == null)
				throw new MalformedJSONException(String.format(
						"An expected value was missing from %s", raw));
		}

		private void initFieldsFromJSONValues() throws UnsupportedLDSchemaException {
			docType = (String) getValue(JSONLDKeys.DOCUMENT_TYPE);
			docSize = (String) getValue(JSONLDKeys.DOCUMENT_SIZE, true);
			docUrl = (String) getValue(JSONLDKeys.DOCUMENT_URL);
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
		 * @see com.nicta.vlabclient.entity.Document#getSize()
		 */
		@Nullable
		public String getSize() throws UnknownValueException {
			return docSize;
		}

		public String getDataUrl() {
			return docUrl;
		}

	}

	private class DocumentDelegator implements Document {
		Document document;

		private DocumentDelegator(String url) {
			this.document = new DocumentImpl(url);
		}

		private DocumentDelegator(Document document) {
			this.document = document;
		}

		@Override
		public String getDataUrl() {
			return document.getDataUrl();
		}

		@Override
		public String getType() throws UnknownValueException {
			return document.getType();
		}

		@Nullable
		@Override
		public String getSize() throws UnknownValueException {
			return document.getSize();
		}

	}

	private class TextDocumentImpl extends DocumentDelegator implements TextDocument {

		private TextDocumentImpl(Document document) {
			super(document);
		}

		private TextDocumentImpl(String url) {
			super(url);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.nicta.vlabclient.entity.TextDocument#rawText()
		 */
		public String rawText() {
			return getTextInvocBuilder(getDataUrl()).get(String.class);
		}
	}

	public class AudioDocumentImpl extends DocumentDelegator implements AudioDocument {

		private AudioDocumentImpl(Document document) {
			super(document);
		}

		public AudioDocumentImpl(String url) {
			super(url);
		}

		public byte[] getData() {
			return getDataInvocBuilder(getDataUrl()).get(byte[].class); // XXX:
																		// not
																		// tested
		}

	}

	private Document getDocInstanceForType(Document doc) {
		if (doc.getType().equals("Audio"))
			return new AudioDocumentImpl(doc);
		else
			return new TextDocumentImpl(doc);
	}


	private abstract class AnnotationImpl extends BasicRestAnnotation {
		private final Map<String, Document> rawDocCache;

		private AnnotationImpl(Map<String, Object> raw, Map<String, Document> docCache)
				throws UnsupportedLDSchemaException {
			ldValues.clear();
			ldValues.putAll(raw);
			rawDocCache = docCache;
			initFieldsFromJSONValues();
		}

		private void initFieldsFromJSONValues() throws UnsupportedLDSchemaException {
			annId = (String) getValue("@id");
			type = (String) getValue(JSONLDKeys.ANNOTATION_TYPE);
			boolean isUri = true;
			try { // XXX: workaround for legacy types which are not yet URIs
				URI uri = new URI(type);
				if (uri.getScheme() == null && uri.getHost() == null)
					isUri = false;
			} catch (URISyntaxException e) {
				isUri = false;
			}
			if (!isUri) {
				// if we're here, what we got didn't look like a URI
				try {
					type = "http://example.org/mock-types/" + URLEncoder.encode(type, "UTF-8");
				} catch (UnsupportedEncodingException e2) {
					throw new RuntimeException(e2);
				}
			}
			label = (String) getValue(JSONLDKeys.ANNOTATION_LABEL, true);
			start = readDouble(getValue(JSONLDKeys.ANNOTATION_START));
			end = readDouble(getValue(JSONLDKeys.ANNOTATION_END));
			valueType = (String) getValue("@type");
			getValue(JSONLDKeys.ANNOTATION_ANNOTATES); // check for key only
		}

		private double readDouble(Object value) {
			try {
				return (Double) value;
			} catch (ClassCastException e) {
				return Double.parseDouble((String) value);
			}
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
			return (String) ldValues.get(JSONLDKeys.ANNOTATION_ANNOTATES);
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
	public Item getItemByUri(String itemUri) throws UnauthorizedAPIKeyException, RestJsonDataException {
		try {
			Object jsonObj = getJsonObject(itemUri);
			Map<String, Object> resolved = getResolvedVersion(jsonObj);
			return new ItemImpl(resolved, itemUri);
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


	@SuppressWarnings("unchecked")
	private Map<String, Object> getResolvedVersion(Object jsonObj) {
		// use 'compact' here to resolve each annotation against the
		// context,
		// rather than to shorten each annotation (which is its primary use)
		try {
			return JsonLdProcessor.compact(jsonObj, EMPTY_MAP, new JsonLdOptions());
		} catch (JsonLdError e) {
			throw new MalformedJSONException(e);
		}
	}

	@SuppressWarnings("unchecked")
	// helper purely to avoid verbose casts
	private Map<String, Object> jmap(Object jsonObject) {
		return (Map<String, Object>) jsonObject;
	}

	private Object getJsonObject(String sourceUrl) {
		try {
			return JSONUtils.fromString(getRawJson(sourceUrl));
		} catch (JsonParseException e) {
			throw new MalformedJSONException(e);
		} catch (JsonMappingException e) {
			throw new MalformedJSONException(e);
		} catch (IOException e) {
			throw new MalformedJSONException(e);
		}
	}

	private String getRawJson(String sourceUrl) {
		return getJsonInvocBuilder(sourceUrl).get(String.class);
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
