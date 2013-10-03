package com.nicta.hls.vlabclient.examples;

import java.util.Map;

import com.nicta.hls.vlabclient.VLabDocument;
import com.nicta.hls.vlabclient.VLabItem;
import com.nicta.hls.vlabclient.VLabItemList;
import com.nicta.hls.vlabclient.VLabRestClient;

public class VLabRestClientExample {
	public static void main(String[] args) {
		String serverUri = args[0];
		String apiKey = args[1];
		String itemListId = args[2];

		VLabRestClient client = new VLabRestClient(serverUri, apiKey);
		try {
			System.out.println(client.getItemListJson(itemListId));
			VLabItemList il = client.getItemList(itemListId);
			System.out.println(String.format("Found %d items", il.numItems())); 
			for (VLabItem ci : il.getCatalogItems()) {
				System.out.println("\nURI:" + ci.getUri());
				System.out.println("\nPRIMARY TEXT:\n" + ci.primaryText());
				System.out.println("\nMETADATA:");
				for (Map.Entry<String, String> entry : ci.getMetadata().entrySet()) {
					System.out.println(entry.getKey() + ": " + entry.getValue());
				}
				System.out.println("\nDOCS:");
				for (VLabDocument doc : ci.documents()) {
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
