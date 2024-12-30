package main.java.tukano.impl.rest;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import main.java.tukano.impl.Token;
import main.java.tukano.impl.rest.auth.RequestCookiesCleanupFilter;
import main.java.tukano.impl.rest.auth.RequestCookiesFilter;
import main.java.utils.IP;


@ApplicationPath("/rest/blobs") // Defines the base URI path for the RESTful API
public class BlobsServer extends Application {
	final private static Logger Log = Logger.getLogger(BlobsServer.class.getName());
	static String SERVER_BASE_URI = "http://%s:%s/blobservice/rest/blobs";
	public static final int PORT = 8081;
	public static String serverURI;
	private Set<Object> singletons = new HashSet<>();
	private Set<Class<?>> resources = new HashSet<>();
			
	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
		serverURI = String.format(SERVER_BASE_URI, IP.hostname(), PORT);
	}

	/**
	 * Constructor for the BlobsServer.
	 * It sets the secret key for the Token and logs the server's URI.
	 */
	public BlobsServer() {
		Token.setSecret("defaultSecret1234567890");
		singletons.add(new RestBlobsResource()); // Handles blob-related API endpoints
		resources.add(RequestCookiesFilter.class);
		resources.add(RequestCookiesCleanupFilter.class);
		resources.add(Authentication.class);

		Log.info(String.format("Blobs Server ready @ %s\n",  serverURI));

	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}
	/**
	 * This method returns the singleton instances of the REST resource classes.
	 * These resources handle various endpoints related to blobs, users, and shorts.
	 *
	 * @return A set of singleton objects that represent the REST resources.
	 */
	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
