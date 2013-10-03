package com.nicta.hls.vlabclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JsonProperty;

class JsonItemList {
	public String getName() {
		return name;
	}

	public int getNumItems() {
		return numItems;
	}

	public String[] getItems() {
		return items;
	}

	private String name;
	
	@JsonProperty(value="num_items")
	private int numItems;
	
	private String[] items;
}

class JsonCatalogItem {
	@JsonProperty(value = "catalog_url")
	private String catalogUrl;
	
	private Map<String, String> metadata;
	
	@JsonProperty(value = "primary_text_url")
	private String primaryTextUrl;
	
	@JsonProperty(value = "annotations_url")
	private String annotationsUrl;
	
	private JsonDocument[] documents;

	public String getCatalogUrl() {
		return catalogUrl;
	}
	public Map<String, String> getMetadata() {
		return metadata;
	}
	public String getPrimaryTextUrl() {
		return primaryTextUrl;
	}
	public JsonDocument[] getDocuments() {
		return documents;
	}
}

class JsonDocument {
	private String url;
	private String type;
	private String size;
	public String getUrl() {
		return url;
	}
	public String getType() {
		return type;
	}
	public String getSize() {
		return size;
	}
}

public class VLabRestClient {

	private String serverBaseUri;
	private String apiKey;
	private Client client;

	/** Construct a new REST client instance
	 * 
	 * @param serverBaseUri The base URI of the server to construct API calls to the JSON REST API.
	 * @param apiKey The API key for the relevant user account, available from the HCS vLab web interface
	 */
	public VLabRestClient(String serverBaseUri, String apiKey) {
		this.serverBaseUri = serverBaseUri;
		this.apiKey = apiKey;
		client = ClientBuilder.newClient();
	}

	/** A representation of an HCS vLab item list. Modelled fairly closely on the JSON REST model,
	 * but at a slightly higher level, with a number of convenience methods to enable 
	 * easier retrieval of related objects, for example
	 * @author andrew.mackinlay
	 *
	 */
	public class ItemList {
		private JsonItemList fromJson;
		private String uri;
		
		private ItemList(JsonItemList raw, String uri) {
			fromJson = raw;
			this.uri = uri;
		}
		
		/** Get the URI which was used to retrieve this item from the REST API */
		public String getUri() {
			return uri;
		}
		
		/** Get the raw URIs for the items from this list.
		 * 
		 * {@link #getCatalogItems()} provides a higher-level interface to the
		 * candidate items which is probably more useful 
		 * @return an array of URIs, stored as strings.
		 */
		public String[] itemUris() {
			return fromJson.getItems();
		}
		
		public String name() {
			return fromJson.getName();
		}
		
		public long numItems() {
			return fromJson.getNumItems();
		}
//		
//		private List<JsonCatalogItem> getJsonCatalogItems() {
//			List<JsonCatalogItem> jcis = new ArrayList<JsonCatalogItem>(numItems());
//				jcis.add(getJsonInvocBuilder(itemUri).get(JsonCatalogItem.class));
//			return jcis;
//		}

		/** Fetch the items associated with this item list */
		public List<CatalogItem> getCatalogItems() {
			List<CatalogItem> cis = new ArrayList<CatalogItem>();
			for (String itemUri : itemUris()) {
				JsonCatalogItem jci = getJsonInvocBuilder(itemUri).get(JsonCatalogItem.class);
				cis.add(new CatalogItem(jci, itemUri));
			}
			return cis;
		}
	}
	
	public class CatalogItem {
		private JsonCatalogItem fromJson;
		private String uri;
		
		private CatalogItem(JsonCatalogItem raw, String uri) {
			fromJson = raw;
			this.uri = uri;
		}
		
		public List<Document> documents() {
			JsonDocument[] jsonDocs = fromJson.getDocuments();
			List<Document> docs = new ArrayList<Document>(jsonDocs.length);
			for (JsonDocument jd : jsonDocs)
				docs.add(new Document(jd));
			return docs;
		}
		
		public String getUri() {
			return uri;
		}
		
		public String getPrimaryTextUrl() {
			return fromJson.getPrimaryTextUrl();
		}
		
		public String primaryText() {
			return getTextInvocBuilder(getPrimaryTextUrl()).get(String.class);
		}
		
		public Map<String, String> getMetadata() {
			return fromJson.getMetadata();
		}
	}

	public class Document {
		private JsonDocument fromJson;
//		private String uri;
		
		private Document(JsonDocument raw) {
			fromJson = raw;
//			this.uri = uri;
		}

		public String getRawTextUrl() {
			return fromJson.getUrl();
		}
		
		public String getType() {
			return fromJson.getType();
		}
		
		public String getSize() {
			return fromJson.getSize();
		}
		
		public String rawText() {
			return getTextInvocBuilder(getRawTextUrl()).get(String.class);
		}
	
	}

	public String getItemListUri(String itemListId) {
		return String.format("%s/item_lists/%s.json", serverBaseUri, itemListId);
	}

	public ItemList getItemList(String itemListId) throws Exception {
		return getItemListFromUri(getItemListUri(itemListId));
	}

	public ItemList getItemListFromUri(String itemListUri) throws Exception {
		return new ItemList(getJsonInvocBuilder(itemListUri).get(JsonItemList.class), itemListUri);
	}

	public String getItemListJson(String itemListId) throws Exception {
		return getItemListJsonFromUri(getItemListUri(itemListId));
	}
	
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
		return client.target(uri)
				.request(contType).accept(contType)
				.header("X-API-KEY", apiKey);
	}

	
	public static void main(String[] args) {
		String serverUri = args[0];
		String apiKey = args[1];
		String itemListId = args[2];

		VLabRestClient client = new VLabRestClient(serverUri, apiKey);
		try {
			System.out.println(client.getItemListJson(itemListId));
			ItemList il = client.getItemList(itemListId);
			System.out.println(String.format("Found %d items", il.numItems())); 
			for (CatalogItem ci : il.getCatalogItems()) {
				System.out.println("\nURI:" + ci.getUri());
				System.out.println("\nPRIMARY TEXT:\n" + ci.primaryText());
				System.out.println("\nMETADATA:");
				for (Map.Entry<String, String> entry : ci.getMetadata().entrySet()) {
					System.out.println(entry.getKey() + ": " + entry.getValue());
				}
				System.out.println("\nDOCS:");
				for (Document doc : ci.documents()) {
					System.out.println("\tTEXT URL: " + doc.getRawTextUrl());
					System.out.println("\tSIZE: " + doc.getSize());
					System.out.println("\tTYPE: " + doc.getType());
					String text = doc.rawText();
					if (text.length() > 5000) 
						text = text.substring(0, 3000) + "…";
					System.out.println("\tCONTENT: " + text);
					System.out.println("\t==================");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
