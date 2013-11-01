package com.nicta.vlabclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.jsonldjava.core.JSONLD;
import com.github.jsonldjava.core.JSONLDProcessingError;
import com.github.jsonldjava.core.Options;
import com.github.jsonldjava.utils.JSONUtils;
import com.nicta.vlabclient.JsonApi.JsonAnnotation;
import com.nicta.vlabclient.JsonApi.JsonAnnotationGroup;
import com.nicta.vlabclient.JsonApi.JsonCatalogItem;
import com.nicta.vlabclient.JsonApi.JsonDocument;
import com.nicta.vlabclient.JsonApi.JsonItemList;
import com.nicta.vlabclient.JsonApi.VersionResult;
import com.nicta.vlabclient.entity.Annotation;
import com.nicta.vlabclient.entity.Annotation.JSONLDKeys;
import com.nicta.vlabclient.entity.AnnotationGroup;
import com.nicta.vlabclient.entity.Document;
import com.nicta.vlabclient.entity.Item;
import com.nicta.vlabclient.entity.ItemList;

/**
 * The primary class to use to interact with the HCS vLab REST API - use this to
 * retrieve items
 * 
 * @author Andrew MacKinlay
 * 
 */
public class RestClient {

	private String serverBaseUri;
	private String apiKey;
	private Client client;
	
	private static Map<String, Object> EMPTY_MAP = new HashMap<String, Object>();

	/**
	 * Construct a new REST client instance
	 * 
	 * @param serverBaseUri
	 *            The base URI of the server to construct API calls to the JSON
	 *            REST API.
	 * @param apiKey
	 *            The API key for the relevant user account, available from the
	 *            HCS vLab web interface
	 * @throws UnknownServerAPIVersionException If the server's reported API version is incompatible with this code
	 */
	public RestClient(String serverBaseUri, String apiKey) throws UnknownServerAPIVersionException {
		this.serverBaseUri = serverBaseUri;
		this.apiKey = apiKey;
		client = ClientBuilder.newClient();
		checkVersion();
	}
	
	private void checkVersion() throws UnknownServerAPIVersionException {
		VersionResult verRes = getJsonInvocBuilder(serverBaseUri + "/version").get(VersionResult.class);
		String version = verRes.apiVersion;
		if (!version.equals("2.0") && !version.startsWith("Sprint_13")) 
			throw new UnknownServerAPIVersionException("This codebase is not designed for API version " + version);
	}

	/**
	 * A representation of an HCS vLab item list which uses the vLab REST client
	 * to retrieve related data
	 * 
	 * @author andrew.mackinlay
	 * 
	 */
	private class ItemListImpl implements ItemList {
		private JsonItemList fromJson;
		private String uri;

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
		public List<Item> getCatalogItems() {
			List<Item> cis = new ArrayList<Item>();
			for (String itemUri : itemUris()) {
				JsonCatalogItem jci = getJsonInvocBuilder(itemUri).get(JsonCatalogItem.class);
				cis.add(new ItemImpl(jci, itemUri));
			}
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
		private JsonCatalogItem fromJson;
		private String uri;

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
			for (JsonDocument jd : jsonDocs)
				docs.add(new DocumentImpl(jd));
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

		public List<Annotation> getAnnotations() {
			return new ArrayList<Annotation>();
		}

		@SuppressWarnings("unchecked")
		public List<Map<String, Object>> annotationsAsJSONLD() {
			Object jsonObj = getJsonObject();
			fixJsonLdContextHack(jsonObj); // XXX: temp hack
			Map<String, Object> compacted = getResolvedVersion(jsonObj);
			Map<String, Object> annWrapper = jmap(compacted.get(JSONLDKeys.ANNOTATION));
			Map<String, Object> commonProps = jmap(compacted.get(JSONLDKeys.COMMON_PROPERTIES));
			List<Object> annObjects = (List<Object>) annWrapper.get("@list");
			List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
			for (Object annObj : annObjects) {
				Map<String, Object> annAsMap = jmap(annObj);
				annAsMap.putAll(commonProps);
				res.add(annAsMap);
			}
			return res;
		}

		private Map<String, Object> getResolvedVersion(Object jsonObj) {
			// use 'compact' here to resolve each annotation against the context,
			// rather than to shorten each annotation (which is its primary use)
			// return (Map<String, Object>) JSONLD.compact(jsonObj, EMPTY_MAP);
			// instead of above, use this workaround for HCSVLAB-654:
			Map<String, Object> jsonAsMap = jmap(jsonObj);
			String ctxUrl = (String) jsonAsMap.remove("@context");
			String ctxString = getJsonInvocBuilder(ctxUrl).get(String.class);
			Map<String, Object> outerCtxMap;
			try {
				outerCtxMap = jmap(JSONUtils.fromString(ctxString));
				Object ctx = outerCtxMap.get("@context");
				jsonAsMap.put("@context", ctx);
				return jmap(JSONLD.compact(jsonAsMap, EMPTY_MAP, new Options("", true)));
			} catch (JsonParseException e) {
				throw new MalformedJSONException(e);
			} catch (JsonMappingException e) {
				throw new MalformedJSONException(e);
			} catch (JSONLDProcessingError e) {
				throw new MalformedJSONException(e);
			}
		}

		/*
		 * Fix (hopefully temp) for problem documented at:
		 * https://jira.intersect.org.au/browse/HCSVLAB-651
		 */
		@SuppressWarnings("unchecked")
		private void fixJsonLdContextHack(Object jsonObject) {
			Map<String, Object> jsonAsMap = jmap(jsonObject);
			Object ctx = jsonAsMap.get("@vocab");
			jsonAsMap.remove("@vocab");
			jsonAsMap.put("@context", ctx);
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
	}

	/**
	 * A class representing a 'document' (version of an item) in the HCSvLab API
	 * 
	 * @author andrew.mackinlay
	 * 
	 */
	private class DocumentImpl implements Document {
		private JsonDocument fromJson;

		private DocumentImpl(JsonDocument raw) {
			fromJson = raw;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.Document#getRawTextUrl()
		 */
		public String getRawTextUrl() {
			return fromJson.getUrl();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.Document#getType()
		 */
		public String getType() {
			return fromJson.getType();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.Document#getSize()
		 */
		public String getSize() {
			return fromJson.getSize();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.Document#rawText()
		 */
		public String rawText() {
			return getTextInvocBuilder(getRawTextUrl()).get(String.class);
		}
	}

	private class AnnotationGroupImpl implements AnnotationGroup {
		private String uri;
		private JsonAnnotationGroup fromJson;

		private AnnotationGroupImpl(JsonAnnotationGroup raw, String uri) {
			this.uri = uri;
			fromJson = raw;
		}

		public String getUri() {
			return uri;
		}

		public String getItemId() {
			return fromJson.getItemId();
		}

		public String getUtteranceUrl() {
			return fromJson.getUtterance();
		}

		public int getNumAnnotations() {
			return fromJson.getAnnotationsFound();
		}

		public List<Annotation> getAnnotations() {
			List<Annotation> anns = new ArrayList<Annotation>();
			for (JsonAnnotation ja : fromJson.getAnnotations())
				anns.add(new AnnotationImpl(ja));
			return anns;
		}
	}

	private class AnnotationImpl implements Annotation {

		private JsonAnnotation fromJson;

		private AnnotationImpl(JsonAnnotation raw) {
			fromJson = raw;
		}

		public String getType() {
			return fromJson.getType();
		}

		public String getLabel() {
			return fromJson.getLabel();
		}

		public float getStart() {
			return fromJson.getStart();
		}

		public float getEnd() {
			return fromJson.getEnd();
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
	 * @throws Exception
	 */
	public ItemList getItemList(String itemListId) throws Exception {
		return getItemListFromUri(getItemListUri(itemListId));
	}

	/**
	 * Get the item list from the supplied rest URI
	 * 
	 * @param itemListUri
	 *            the fully qualified URI for the REST API
	 * @return the item list object with the given ID
	 * @throws Exception
	 * @see #getItemList(String)
	 */
	public ItemList getItemListFromUri(String itemListUri) throws Exception {
		return new ItemListImpl(getJsonInvocBuilder(itemListUri).get(JsonItemList.class),
				itemListUri);
	}

	/**
	 * Get the raw JSON for the item list from the supplied ID. Mostly useful
	 * for debugging
	 * 
	 * @param itemListId
	 *            the ID of the item list
	 * @return unformatted JSON from the HCS vLab REST server
	 * @throws Exception
	 * @see #getItemList(String)
	 */
	public String getItemListJson(String itemListId) throws Exception {
		return getItemListJsonFromUri(getItemListUri(itemListId));
	}

	/**
	 * Get the raw JSON for the item list from the supplied URI. Mostly useful
	 * for debugging
	 * 
	 * @param itemListUri
	 *            the fully qualified URI for the REST API
	 * @return unformatted JSON from the HCS vLab REST server
	 * @throws Exception
	 * @see RestClient#getItemListJson(String)
	 */
	public String getItemListJsonFromUri(String itemListUri) throws Exception {
		return getJsonInvocBuilder(itemListUri).get(String.class);
	}

	private Invocation.Builder getJsonInvocBuilder(String uri) {
		return getInvocBuilder(uri, MediaType.APPLICATION_JSON);
	}

	private Invocation.Builder getTextInvocBuilder(String uri) {
		return getInvocBuilder(uri, MediaType.TEXT_PLAIN);
	}

	private Invocation.Builder getInvocBuilder(String uri, String contType) {
		return client.target(uri).request(contType).accept(contType).header("X-API-KEY", apiKey);
	}
}
