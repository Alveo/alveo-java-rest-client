package au.edu.alveo.client.examples;

import java.util.Map;

import au.edu.alveo.client.entity.InvalidServerAddressException;
import au.edu.alveo.client.RestClient;
import au.edu.alveo.client.entity.UnauthorizedAPIKeyException;
import au.edu.alveo.client.entity.Annotation;
import au.edu.alveo.client.entity.EntityNotFoundException;
import au.edu.alveo.client.entity.Item;
import au.edu.alveo.client.entity.ItemList;
import au.edu.alveo.client.entity.TextDocument;

public class RestClientExample {
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println("Invalid number of parameters: " + args.length + " (should be 3)");
			usage();
			return;
		}
		String serverUri = args[0];
		String apiKey = args[1];
		String itemListId = args[2];
		try {
			RestClient client = new RestClient(serverUri, apiKey);
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
				// for (Annotation ann : ci.getAnnotations())
				// System.out.println(String.format("\t%s(%s)@<%1.1f:%1.1f>",
				// ann.getType(), ann.getLabel(),
				// ann.getStart(), ann.getEnd()));
				for (Annotation ann : ci.getTextAnnotations()) {
					System.out.println("\t" + ann);
				}
				for (Map<String, Object> ann : ci.annotationsAsJSONLD()) {
					for (Map.Entry<String, Object> annEntry : ann.entrySet())
						System.out.println(String.format("\t%s:%s", annEntry.getKey(),
								annEntry.getValue()));
					System.out.println("\t,");
				}
			}
		} catch (EntityNotFoundException e) {
			System.err.println("Item list with ID " + itemListId
					+ " was not found; please check the parameters are ordered correctly");
			usage();
			e.printStackTrace(System.err);
		} catch (UnauthorizedAPIKeyException e) {
			System.err.println("The server did not accept the provided API key '" + apiKey
					+ "'; please check the parameters are ordered correctly");
			usage();
			e.printStackTrace(System.err);
		} catch (InvalidServerAddressException e) {
			System.err.println("The server address '" + serverUri
					+ "' was invalid; please check the parameters are ordered correctly");
			usage();
			e.printStackTrace(System.err);
		}
	}

	public static void usage() {
		System.err.println("Usage: " + RestClientExample.class.getName()
				+ " serverUri apiKey itemListId");
		System.err
				.println("    Retrieves an item list from the server and displays the documents along with");
		System.err.println("    the associated annotations.\n");
	}
}
