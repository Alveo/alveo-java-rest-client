package com.nicta.vlabclient;

import java.util.List;

import com.nicta.vlabclient.entity.TextAnnotation;
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

@RunWith(JUnit4.class)
public class RestClientTest {
	/**
	 * Class to test the Rest Client.
	 * 
	 * Uses Betamax for offline-testing. Be very careful of licensing issues when
	 * recording actual corpora -- if you write any new data, make sure
	 * to remove any copyrighted data using the utilities in
	 * com/nicta/vlabclient/utils/SanitizeTapeData.scala
	 */

	@Rule
	public Recorder recorder = new Recorder();

	@Betamax(tape = "standard_test")
	@Test
	public void fetchItemList() throws RestClientException {
		RestClient rc = newRestClient();
		ItemList il = rc.getItemList("45");
		Assert.assertEquals(2, il.numItems());
		List<Item> items = il.getCatalogItems();
		Assert.assertEquals(il.numItems(), items.size());
		Assert.assertEquals(items.get(0).getUri(), rc.getItem("hcsvlab:283").getUri());
		Item item0 = items.get(0);
		String item0text = item0.primaryText();
		Assert.assertTrue(item0text.startsWith("um palm"));
		Assert.assertTrue(item0text.endsWith("ice swab"));
	}

	@Betamax(tape = "standard_test")
	@Test
	public void fetchAnnotations() throws RestClientException {
		RestClient rc = newRestClient();
		Item item = rc.getItem("hcsvlab:283");
		List<Annotation> anns = item.getAnnotations();
		Assert.assertTrue(anns.size() > 20);
	}

	@Betamax(tape = "standard_test")
	@Test
	public void checkAnnotations() throws RestClientException {
		RestClient rc = newRestClient();
		Item item = rc.getItem("hcsvlab:283");
		List<Annotation> anns = item.getAnnotations();
		TextAnnotation firstSpkrAnn = (TextAnnotation) anns.get(9);
		Assert.assertEquals(0, firstSpkrAnn.getStartOffset());
		Assert.assertEquals(134, firstSpkrAnn.getEndOffset());
		String docText = item.primaryText();
		String annText = docText.substring(firstSpkrAnn.getStartOffset(), firstSpkrAnn.getEndOffset());
		Assert.assertTrue(annText.startsWith("um palm") && annText.replaceAll(" *$", "").endsWith(" gi"));
		Assert.assertEquals(firstSpkrAnn.getType(), "speaker");
		TextAnnotation lastAnn = (TextAnnotation) anns.get(anns.size() - 1);
		Assert.assertEquals(docText.length(), lastAnn.getEndOffset());
	}

	
	private RestClient newRestClient() throws RestClientException {
		return new RestClient("http://ic2-hcsvlab-staging2-vm.intersect.org.au/", "FAKE_API_KEY");
	}

}
