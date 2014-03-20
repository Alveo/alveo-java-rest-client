package com.nicta.vlabclient;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** A class to hold struct-like classes to represent JSON data from
 * the HCS vLab web API
 * 
 * Package visibility as these are not designed for external use
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


	static class JsonDocument {
		private String url;
		private String type;
		private String size;
		public String getUrl() {
			return url;
		}
		
		@JsonProperty(value = "dc:type")
		public String getType() {
			return type;
		}
		public String getSize() {
			return size;
		}
	}

	
	static class VersionResult {
		@JsonProperty(value = "API version")
		public String apiVersion;
	}
}
