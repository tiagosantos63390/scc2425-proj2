package main.java.tukano.impl.rest;

import jakarta.inject.Singleton;
import main.java.tukano.api.Blobs;
import main.java.tukano.api.rest.RestBlobs;
import main.java.tukano.impl.JavaBlobs;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class provides the REST endpoints for managing blobs (files) through HTTP.
 */
@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {

	final Blobs impl; // The implementation of the Blobs interface to interact with actual blob storage
	final String ADMIN = "admin";

	/**
	 * Constructor to initialize the RestBlobsResource class.
	 * It uses the JavaBlobs class as the actual implementation for the blob operations.
	 */
	public RestBlobsResource() {
		this.impl = JavaBlobs.getInstance();
	}

	/**
	 * Uploads a blob to the server.
	 *
	 * @param blobId The unique identifier for the blob.
	 * @param bytes The binary content of the blob to be uploaded.
	 * @param token The token required for authentication.
	 */
	@Override
	public void upload(String blobId, byte[] bytes, String token) {
		Authentication.validateSession("");
		super.resultOrThrow( impl.upload(blobId, bytes, token));
	}

	/**
	 * Downloads a blob from the server.
	 *
	 * @param blobId The unique identifier for the blob.
	 * @param token The token required for authentication.
	 * @return The binary content of the downloaded blob.
	 */
	@Override
	public byte[] download(String blobId, String token) {
		Authentication.validateSession("");
		return super.resultOrThrow( impl.download( blobId, token ));
	}

	/**
	 * Deletes a specific blob from the server.
	 *
	 * @param blobId The unique identifier for the blob to be deleted.
	 * @param token The token required for authentication.
	 */
	@Override
	public void delete(String blobId, String token) {
		Authentication.validateSession(ADMIN);
		super.resultOrThrow( impl.delete( blobId, token ));
	}

	/**
	 * Deletes all blobs associated with a user.
	 *
	 * @param userId The user identifier whose blobs will be deleted.
	 * @param password The password for authentication.
	 */
	@Override
	public void deleteAllBlobs(String userId, String password) {
		Authentication.validateSession(ADMIN);
		super.resultOrThrow( impl.deleteAllBlobs( userId, password ));
	}
}
