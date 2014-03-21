package com.nicta.vlabclient;

import com.nicta.vlabclient.entity.Annotation;
import com.nicta.vlabclient.entity.HCSvLabException;
import com.nicta.vlabclient.entity.Item;
import com.nicta.vlabclient.entity.ItemList;
import com.nicta.vlabclient.entity.TextAnnotation;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by amack on 20/03/14.
 */
public abstract class RestClientBaseTest {

	public RestClientBaseTest() throws HCSvLabException {
		// can't initialise the rest client here due to
		// using betamax in testing. Recreating the client for every call is not
		// recommended for general use though
	}

	static Config getConfig() {
		return ConfigFactory.load().getConfig("vlabclient");
	}

	@Test
	public void fetchItemList() throws HCSvLabException {
		ItemList il = newRestClient().getItemList("45");
		Assert.assertEquals(2, il.numItems());
		List<Item> items = il.getCatalogItems();
		Assert.assertEquals(il.numItems(), items.size());
		Assert.assertEquals(items.get(1).getUri(), newRestClient().getItem("gcsause:GCSAusE07").getUri());
	}

	@Test
	public void fetchItem() throws HCSvLabException {
		Item item0 = newRestClient().getItem("gcsause:GCSAusE07");
		String item0text = item0.primaryText();
		Assert.assertTrue(item0text.startsWith("Andy’s starting"));
		Assert.assertTrue(item0text.endsWith("know"));
	}

	@Test
	public void fetchAnnotations() throws HCSvLabException {
		Item item = newRestClient().getItem("gcsause:GCSAusE07");
		List<Annotation> anns = item.getAnnotations();
		Assert.assertTrue(anns.size() > 20);
	}

	@Test
	public void checkAnnotations() throws HCSvLabException {
		Item item = newRestClient().getItem("gcsause:GCSAusE07");
		List<Annotation> anns = item.getAnnotations();
		TextAnnotation firstSpkrAnn = (TextAnnotation) anns.get(9);
		Assert.assertEquals(93, firstSpkrAnn.getStartOffset());
		Assert.assertEquals(113, firstSpkrAnn.getEndOffset());
		String docText = item.primaryText();
		String annText = docText.substring(firstSpkrAnn.getStartOffset(), firstSpkrAnn.getEndOffset());
		Assert.assertTrue(annText.startsWith("having") && annText.replaceAll(" *$", "").endsWith("nap"));
		Assert.assertEquals(firstSpkrAnn.getType(), "speaker");
		TextAnnotation lastAnn = (TextAnnotation) anns.get(anns.size() - 1);
		Assert.assertEquals(docText.length(), lastAnn.getEndOffset());
	}

	@Test
	public void uploadAnnotations() throws HCSvLabException {
		Item i0 = newRestClient().getItem("gcsause:GCSAusE07");
		List<Annotation> anns = new ArrayList<Annotation>();
		anns.add(new TextRestAnnotation("entity", "proper-name", 0, 4));
		anns.add(new TextRestAnnotation("pos", "NNP", 0, 4));
		// need different contents on successive runs or we get a duplicate file error
		anns.add(new TextRestAnnotation("comment", "comment at time " + currDate(), 0, 4));
		i0.storeNewAnnotations(anns);
	}

	@Test
	public void createRestClient() throws HCSvLabException {
		newRestClient();
	}

	private static String currDate() {
		return String.format("%tFT%<tRZ", new Date());
	}

	protected abstract RestClient newRestClient() throws HCSvLabException;

	static String liveServerBase() {
		return getConfig().getString("test.server-base");
	}

	static String liveApiKey() {
		return getConfig().getString("test.api-key");
	}


}
