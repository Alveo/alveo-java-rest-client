package au.edu.alveo.client;

import au.edu.alveo.client.entity.HCSvLabException;
import com.typesafe.config.ConfigException;
import org.junit.Assume;
import org.junit.BeforeClass;
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

	@BeforeClass
	public static void precheck() {
		// ignore live tests unless we've asked for them
		Assume.assumeTrue(shouldRun());
	}

	private static boolean shouldRun() {
		try {
			return getConfig().getBoolean("test.run-live");
		} catch (ConfigException e) {
			return false; // don't run live tests by default
		}
	}

	private boolean shouldWriteToServer() {
		try {
			return getConfig().getBoolean("test.write-to-live-server");
		} catch (ConfigException e) {
			return false; // don't write to live server by default
		}
	}

	public RestClientLiveTest() throws HCSvLabException {
		super();
	}

	@Override
	public void uploadAnnotations() throws HCSvLabException {
		Assume.assumeTrue(shouldWriteToServer());
		super.uploadAnnotations();
	}

	protected RestClient newRestClient() throws HCSvLabException {
		try {
			return new RestClient(liveServerBase(), liveApiKey());
		} catch (ConfigException e) {
			LOG.error("Error reading test configuration. Configure for testing according to the instructions in the README.");
			throw e;
		}
	}

}
