package com.nicta.vlabclient.examples;

import java.util.Map;

import com.nicta.vlabclient.RestClient;
import com.nicta.vlabclient.entity.Annotation;
import com.nicta.vlabclient.entity.Document;
import com.nicta.vlabclient.entity.Item;
import com.nicta.vlabclient.entity.ItemList;
import com.nicta.vlabclient.entity.TextDocument;

public class RestClientExample {
	public static void main(String[] args) throws Exception {
		String serverUri = args[0];
		String apiKey = args[1];
		String itemListId = args[2];

		RestClient client = new RestClient(serverUri, apiKey);
		try {
			System.out.println(client.getItemListJson(itemListId));
			ItemList il = client.getItemList(itemListId);
			System.out.println(String.format("Found %d items", il.numItems())); 
			for (Item ci : il.getCatalogItems()) {
				System.out.println("\nURI:" + ci.getUri());
				System.out.println("\nPRIMARY TEXT:\n" + ci.primaryText());
				System.out.println("\nMETADATA:");
				for (Map.Entry<String, String> entry : ci.getMetadata().entrySet()) {
					System.out.println(entry.getKey() + ": " + entry.getValue());
				}
				System.out.println("\nDOCS:");
				for (TextDocument doc : ci.textDocuments()) {
					System.out.println("\tTEXT URL: " + doc.getDataUrl());
					System.out.println("\tSIZE: " + doc.getSize());
					System.out.println("\tTYPE: " + doc.getType());
					String text = doc.rawText();
					if (text.length() > 5000) 
						text = text.substring(0, 3000) + "…";
					System.out.println("\tCONTENT: " + text);
					System.out.println("\t==================");
				}
				System.out.println("\nANNS:");
//				for (Annotation ann : ci.getAnnotations()) 
//					System.out.println(String.format("\t%s(%s)@<%1.1f:%1.1f>", ann.getType(), ann.getLabel(),
//							ann.getStart(), ann.getEnd()));
//				for (Map<String, Object> ann : ci.annotationsAsJSONLD()) {
//					for (Map.Entry<String, Object> annEntry : ann.entrySet()) 
//						System.out.println(String.format("\t%s:%s", annEntry.getKey(), annEntry.getValue()));
//					System.out.println("\t,");
//				}
				for (Annotation ann : ci.getTextAnnotations()) {
					System.out.println("\t" + ann);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
