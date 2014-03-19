package com.nicta.vlabclient;

import java.util.ArrayList;
import java.util.List;

import com.nicta.vlabclient.entity.TextAnnotation;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import co.freeside.betamax.Betamax;
import co.freeside.betamax.Recorder;

import com.nicta.vlabclient.entity.Annotation;
import com.nicta.vlabclient.entity.Item;
import com.nicta.vlabclient.entity.ItemList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class RestClientTest extends RestClientBaseTest {
	/**
	 * Class to test the Rest Client.
	 * 
	 * Uses Betamax for offline-testing. Be very careful of licensing issues when
	 * recording actual corpora -- if you write any new data, make sure
	 * to remove any copyrighted data using the utilities in
	 * com/nicta/vlabclient/utils/SanitizeTapeData.scala
	 */

	private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

	@Rule
	public Recorder recorder = new Recorder();
	private final RestClient restClient;

	public RestClientTest() throws RestClientException {
		restClient = newRestClient();
	}

	//	@Betamax(tape = "standard_test")
	@Test
	public void fetchItemList() throws RestClientException {
		ItemList il = restClient.getItemList("45");
		Assert.assertEquals(2, il.numItems());
		List<Item> items = il.getCatalogItems();
		Assert.assertEquals(il.numItems(), items.size());
		Assert.assertEquals(items.get(1).getUri(), restClient.getItem("gcsause:GCSAusE07").getUri());
	}

	@Test
	public void fetchItem() throws RestClientException {
		Item item0 = restClient.getItem("gcsause:GCSAusE07");
		String item0text = item0.primaryText();
		Assert.assertTrue(item0text.startsWith("Andy’s starting"));
		Assert.assertTrue(item0text.endsWith("know"));
	}

//	@Betamax(tape = "standard_test")
	@Test
	public void fetchAnnotations() throws RestClientException {
		Item item = restClient.getItem("gcsause:GCSAusE07");
		List<Annotation> anns = item.getAnnotations();
		Assert.assertTrue(anns.size() > 20);
	}

//	@Betamax(tape = "standard_test")
	@Test
	public void checkAnnotations() throws RestClientException {
		Item item = restClient.getItem("gcsause:GCSAusE07");
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

//	@Betamax(tape = "standard_test")
	@Test
	public void uploadAnnotations() throws RestClientException {
		Item i0 = restClient.getItem("gcsause:GCSAusE07");
		List<Annotation> anns = new ArrayList<Annotation>();
		anns.add(new TextRestAnnotation("entity", "proper-name", 0, 4));
		anns.add(new TextRestAnnotation("pos", "NNP", 0, 4));
		i0.storeNewAnnotations(anns);
	}

	
	private RestClient newRestClient() throws RestClientException {
		try {
			return new RestClient(serverBase(), apiKey());
		} catch (ConfigException e) {
			LOG.error("Error reading test configuration. Configure for testing according to the instructions in the README.");
			throw e;
		}
	}


	static Config getConfig() {
		return ConfigFactory.load().getConfig("vlabclient");
	}

	static String serverBase() {
		return getConfig().getString("test.server-base");
	}

	static String apiKey() {
		return getConfig().getString("test.api-key");
	}

}
