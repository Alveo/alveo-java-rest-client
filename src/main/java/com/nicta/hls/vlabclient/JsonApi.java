package com.nicta.hls.vlabclient;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/** A class to hold struct-like classes to represent JSON data from
 * the HCS vLab web API
 * @author amack
 *
 */
class JsonApi {
	static class JsonItemList {
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
		
		// property annotation needed to avoid unknown property error
		// (even if we call the field 'num_items')
		@JsonProperty(value="num_items")
		private int numItems;
		
		private String[] items;
	}

	static class JsonCatalogItem {
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

	static class JsonDocument {
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
}
