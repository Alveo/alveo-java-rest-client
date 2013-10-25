package com.nicta.vlabclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import com.nicta.vlabclient.JsonApi.JsonAnnotationGroup;

import static com.nicta.vlabclient.JsonApi.*;

/**
 * The primary class to use to interact with the HCS vLab REST API - use this to retrieve items
 * 
 * @author Andrew MacKinlay
 * 
 */
public class RestClient {

	private String serverBaseUri;
	private String apiKey;
	private Client client;

	/**
	 * Construct a new REST client instance
	 * 
	 * @param serverBaseUri
	 *            The base URI of the server to construct API calls to the JSON
	 *            REST API.
	 * @param apiKey
	 *            The API key for the relevant user account, available from the
	 *            HCS vLab web interface
	 */
	public RestClient(String serverBaseUri, String apiKey) {
		this.serverBaseUri = serverBaseUri;
		this.apiKey = apiKey;
		client = ClientBuilder.newClient();
	}

	/**
	 * A representation of an HCS vLab item list which uses the vLab REST client
	 * to retrieve related data
	 * 
	 * @author andrew.mackinlay
	 * 
	 */
	public class ItemList implements VLabItemList {
		private JsonItemList fromJson;
		private String uri;

		private ItemList(JsonItemList raw, String uri) {
			fromJson = raw;
			this.uri = uri;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.VLabItemList#getUri()
		 */
		public String getUri() {
			return uri;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.VLabItemList#itemUris()
		 */
		public String[] itemUris() {
			return fromJson.getItems();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.VLabItemList#name()
		 */
		public String name() {
			return fromJson.getName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.VLabItemList#numItems()
		 */
		public int numItems() {
			return fromJson.getNumItems();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.VLabItemList#getCatalogItems()
		 */
		public List<VLabItem> getCatalogItems() {
			List<VLabItem> cis = new ArrayList<VLabItem>();
			for (String itemUri : itemUris()) {
				JsonCatalogItem jci = getJsonInvocBuilder(itemUri).get(JsonCatalogItem.class);
				cis.add(new CatalogItem(jci, itemUri));
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
	public class CatalogItem implements VLabItem {
		private JsonCatalogItem fromJson;
		private String uri;

		private CatalogItem(JsonCatalogItem raw, String uri) {
			fromJson = raw;
			this.uri = uri;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.VLabItem#documents()
		 */
		public List<Document> documents() {
			JsonDocument[] jsonDocs = fromJson.getDocuments();
			List<Document> docs = new ArrayList<Document>(jsonDocs.length);
			for (JsonDocument jd : jsonDocs)
				docs.add(new Document(jd));
			return docs;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.VLabItem#getUri()
		 */
		public String getUri() {
			return uri;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.VLabItem#getPrimaryTextUrl()
		 */
		public String getPrimaryTextUrl() {
			return fromJson.getPrimaryTextUrl();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.VLabItem#primaryText()
		 */
		public String primaryText() {
			return getTextInvocBuilder(getPrimaryTextUrl()).get(String.class);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.VLabItem#getMetadata()
		 */
		public Map<String, String> getMetadata() {
			return fromJson.getMetadata();
		}
		
		public VLabAnnotationGroup getAnnotationGroup() {
			String url = fromJson.getAnnotationsUrl();
			return new AnnotationGroup(getJsonInvocBuilder(url).get(JsonAnnotationGroup.class), url);
		}

		public List<VLabAnnotation> getAnnotations() {
			return getAnnotationGroup().getAnnotations();
		}
	}

	/**
	 * A class representing a 'document' (version of an item) in the HCSvLab API
	 * 
	 * @author andrew.mackinlay
	 * 
	 */
	public class Document implements VLabDocument {
		private JsonDocument fromJson;

		private Document(JsonDocument raw) {
			fromJson = raw;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.VLabDocument#getRawTextUrl()
		 */
		public String getRawTextUrl() {
			return fromJson.getUrl();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.VLabDocument#getType()
		 */
		public String getType() {
			return fromJson.getType();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.VLabDocument#getSize()
		 */
		public String getSize() {
			return fromJson.getSize();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nicta.vlabclient.VLabDocument#rawText()
		 */
		public String rawText() {
			return getTextInvocBuilder(getRawTextUrl()).get(String.class);
		}
	}
	
	public class AnnotationGroup implements VLabAnnotationGroup {
		private String uri;
		private JsonAnnotationGroup fromJson;

		private AnnotationGroup(JsonAnnotationGroup raw, String uri) {
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

		public List<VLabAnnotation> getAnnotations() {
			List<VLabAnnotation> anns = new ArrayList<VLabAnnotation>();
			for (JsonAnnotation ja : fromJson.getAnnotations()) 
				anns.add(new Annotation(ja));
			return anns;
		}
	}

	public class Annotation implements VLabAnnotation {

		private JsonAnnotation fromJson;

		private Annotation(JsonAnnotation raw) {
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
	 * {@link #getItemList()} is probably more useful
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
	public VLabItemList getItemList(String itemListId) throws Exception {
		return getItemListFromUri(getItemListUri(itemListId));
	}

	/**
	 * Get the item list from the supplied rest URI
	 * 
	 * @param itemListUri
	 *            the fully qualified URI for the REST API
	 * @return the item list object with the given ID
	 * @throws Exception
	 * @see {@link #getItemList(String)}
	 */
	public VLabItemList getItemListFromUri(String itemListUri) throws Exception {
		return new ItemList(getJsonInvocBuilder(itemListUri).get(JsonItemList.class), itemListUri);
	}

	/**
	 * Get the raw JSON for the item list from the supplied ID. Mostly useful
	 * for debugging
	 * 
	 * @param itemListId
	 *            the ID of the item list
	 * @return unformatted JSON from the HCS vLab REST server
	 * @throws Exception
	 * @see {@link #getItemList(String)}
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
	 * @see {@link #getItemListJson(String)}
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
