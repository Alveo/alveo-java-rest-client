package com.nicta.vlabclient;

import co.freeside.betamax.Betamax;
import co.freeside.betamax.Recorder;
import co.freeside.betamax.TapeMode;
import com.nicta.vlabclient.entity.HCSvLabException;
import com.typesafe.config.ConfigException;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by amack on 20/03/14.
 */
@RunWith(JUnit4.class)
public class RestClientRecordedTest extends RestClientBaseTest {
	/**
	 * Class to test the Rest Client.
	 * <p/>
	 * Uses Betamax for offline-testing.
	 * <p/>
	 * To record new entries (eg after a server API change), add the config item
	 * "vlabclient.test.record-new" with value "true" to your
	 * application.conf. If this is not found, it will be read only,
	 * to avoid accidentally writing new license-encumbered
	 * data to the repository.
	 * <p/>
	 * Be very careful of licensing issues when
	 * recording actual corpora -- if you write any new data, make sure
	 * to remove any copyrighted data using the utilities in
	 * com/nicta/vlabclient/utils/SanitizeTapeData.scala; in addition,
	 * you'll need to replace your API key with the text FAKE_API_KEY
	 *
	 */

	private static final Logger LOG = LoggerFactory.getLogger(RestClientRecordedTest.class);

	@Rule
	public Recorder recorder = new Recorder();

	@BeforeClass
	public static void precheck() {
		// ignore recorded tests if we request it
		Assume.assumeTrue(shouldRun());
	}

	private static boolean shouldRun() {
		try {
			return getConfig().getBoolean("test.run-recorded");
		} catch (ConfigException e) {
			return true; // only run recorded tests by default
		}
	}


	public RestClientRecordedTest() throws HCSvLabException {
		super();
		recorder.setDefaultMode(inRecordMode() ? TapeMode.READ_WRITE : TapeMode.READ_ONLY);
	}

	@Betamax(tape = "standard_test")
	@Test
	public void fetchItemList() throws HCSvLabException {
		super.fetchItemList();
	}

	@Betamax(tape = "standard_test")
	@Test
	public void fetchItem() throws HCSvLabException {
		super.fetchItem();
	}

	@Betamax(tape = "standard_test")
	@Test
	public void fetchAnnotations() throws HCSvLabException {
		super.fetchAnnotations();
	}

	@Betamax(tape = "standard_test")
	@Test
	public void checkAnnotations() throws HCSvLabException {
		super.checkAnnotations();
	}

	@Betamax(tape = "standard_test")
	@Test
	public void uploadAnnotations() throws HCSvLabException {
		super.uploadAnnotations();
	}

	@Betamax(tape = "standard_test")
	@Test
	public void createRestClient() throws HCSvLabException {
		super.createRestClient();
	}

	protected RestClient newRestClient() throws HCSvLabException {
		try {
			if (inRecordMode())
				return new RestClient(liveServerBase(), liveApiKey());
			else
				return new RestClient("http://ic2-hcsvlab-staging2-vm.intersect.org.au/", "FAKE_API_KEY");
		} catch (ConfigException e) {
			LOG.error("Error reading test configuration. Configure for testing according to the instructions in the README.");
			throw e;
		}
	}


	private static boolean inRecordMode() {
		try {
			return getConfig().getBoolean("test.record-new");
		} catch (ConfigException.Missing e) {
			return false;
		}
	}


}
