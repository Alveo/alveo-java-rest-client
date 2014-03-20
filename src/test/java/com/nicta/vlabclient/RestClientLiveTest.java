package com.nicta.vlabclient;

import com.typesafe.config.ConfigException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class RestClientLiveTest extends RestClientBaseTest {
	/**
	 * Class to test the Rest Client using a live vLab instance
	 */

	private static final Logger LOG = LoggerFactory.getLogger(RestClientLiveTest.class);

	public RestClientLiveTest() throws RestClientException {
		super();
	}


	@Test
	public void fetchItem() throws RestClientException {
		super.fetchItem();
	}

	@Test
	public void fetchAnnotations() throws RestClientException {
		super.fetchAnnotations();
	}

	@Test
	public void checkAnnotations() throws RestClientException {
		super.checkAnnotations();
	}

	@Test
	public void uploadAnnotations() throws RestClientException {
		super.uploadAnnotations();
	}


	protected RestClient newRestClient() throws RestClientException {
		try {
			return new RestClient(liveServerBase(), liveApiKey());
		} catch (ConfigException e) {
			LOG.error("Error reading test configuration. Configure for testing according to the instructions in the README.");
			throw e;
		}
	}

}
