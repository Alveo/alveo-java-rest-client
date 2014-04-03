package com.nicta.vlabclient.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 *
 * Temporary workaround class - should not be needed in the future
 */
public class TypeUriFixer {
	public static String convertToUriIfNeeded(String possUri) {
		boolean isUri = true;
		try { // XXX: workaround for legacy types which are not yet URIs
			URI uri = new URI(possUri);
			if (uri.getScheme() == null && uri.getHost() == null)
				isUri = false;
		} catch (URISyntaxException e) {
			isUri = false;
		}
		if (!isUri) {
			// if we're here, what we got didn't look like a URI
			try {
				possUri = "http://hcsvlab.org/bare-types/" + URLEncoder.encode(possUri, "UTF-8");
			} catch (UnsupportedEncodingException e2) {
				throw new RuntimeException(e2);
			}
		}
		return possUri;
	}
}
