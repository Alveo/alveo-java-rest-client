package com.nicta.hls.vlabclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import static com.nicta.hls.vlabclient.JsonApi.*;

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
	
	/** A representation of an Item from a item list in the HCS vLab REST API
	 * 
	 * @author andrew.mackinlay
	 *
	 */
	public class CatalogItem {
		private JsonCatalogItem fromJson;
		private String uri;
		
		private CatalogItem(JsonCatalogItem raw, String uri) {
			fromJson = raw;
			this.uri = uri;
		}
		
		/** Return the documents associated with this item */
		public List<Document> documents() {
			JsonDocument[] jsonDocs = fromJson.getDocuments();
			List<Document> docs = new ArrayList<Document>(jsonDocs.length);
			for (JsonDocument jd : jsonDocs)
				docs.add(new Document(jd));
			return docs;
		}
		
		/** Return the URI (a valid REST URI) from which this item was retrieved */
		public String getUri() {
			return uri;
		}
		
		/** Get the URL at which the primary text of the item is stored;
		 * 
		 * Library users will probably be more interested in the {@link #primaryText()} method
		 * which does the text retrieval automatically.
		 * @return a URL storing the primary text
		 */
		public String getPrimaryTextUrl() {
			return fromJson.getPrimaryTextUrl();
		}
		
		/** Get the primary text of the item 
		 * 
		 * @return the primary text associated with an item
		 */
		public String primaryText() {
			return getTextInvocBuilder(getPrimaryTextUrl()).get(String.class);
		}
		
		/** Get the metadata associated with an item
		 * 
		 * @return a mapping from all metadata key names to metadata values
		 */
		public Map<String, String> getMetadata() {
			return fromJson.getMetadata();
		}
	}

	/** A class representing a 'document' (version of an item) in the HCSvLab API
	 * 
	 * @author andrew.mackinlay
	 *
	 */
	public class Document {
		private JsonDocument fromJson;
		
		private Document(JsonDocument raw) {
			fromJson = raw;
		}
		
		/** Get the URL where the raw text is stored. 
		 * 
		 * The {@link #rawText()} method is probably more useful to end users
		 * 
		 * @return The URL from which the raw text can be retrieved
		 */
		public String getRawTextUrl() {
			return fromJson.getUrl();
		}
		
		/** Get the 'type' of the document
		 * 
		 * @return The document type according to vLab, indicating how the document relates to the item,
		 *  such as "Original", "Raw" or "Text"
		 */
		public String getType() {
			return fromJson.getType();
		}
		
		/** Get the document size
		 * 
		 * @return A string representation of the document size, such as "1.8kB"
		 */
		public String getSize() {
			return fromJson.getSize();
		}
		
		/** Get the raw document text 
		 * 
		 * @return The raw text of the document.
		 */
		public String rawText() {
			return getTextInvocBuilder(getRawTextUrl()).get(String.class);
		}
	
	}

	/** Return the URI of the item list given an ID. For end-users, {@link #getItemList()} is probably 
	 *  more useful
	 * 
	 * @param itemListId the ID of the item list
	 * @return a URI stored as a string
	 */
	public String getItemListUri(String itemListId) {
		return String.format("%s/item_lists/%s.json", serverBaseUri, itemListId);
	}

	/** Get the item list with the supplied ID
	 * 
	 * @param itemListId the ID of the item list
	 * @return the item list object with the given ID
	 * @throws Exception
	 */
	public ItemList getItemList(String itemListId) throws Exception {
		return getItemListFromUri(getItemListUri(itemListId));
	}

	/** Get the item list from the supplied rest URI
	 * 
	 * @param itemListUri the fully qualified URI for the REST API
	 * @return the item list object with the given ID
	 * @throws Exception
	 * @see {@link #getItemList(String)}
	 */
	public ItemList getItemListFromUri(String itemListUri) throws Exception {
		return new ItemList(getJsonInvocBuilder(itemListUri).get(JsonItemList.class), itemListUri);
	}

	/** Get the raw JSON for the item list from the supplied ID. Mostly useful for debugging
	 * 
	 * @param itemListId the ID of the item list
	 * @return unformatted JSON from the HCS vLab REST server
	 * @throws Exception
	 * @see {@link #getItemList(String)}
	 */
	public String getItemListJson(String itemListId) throws Exception {
		return getItemListJsonFromUri(getItemListUri(itemListId));
	}
	
	/** Get the raw JSON for the item list from the supplied URI. Mostly useful for debugging
	 * 
	 * @param itemListUri the fully qualified URI for the REST API
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
