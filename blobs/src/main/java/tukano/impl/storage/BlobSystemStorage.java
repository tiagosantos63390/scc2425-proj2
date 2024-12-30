package main.java.tukano.impl.storage;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import main.java.tukano.api.Result;

public class BlobSystemStorage implements BlobStorage {
    private final String storageRoot;

    /**
     * Constructor for BlobSystemStorage.
     */
    public BlobSystemStorage() {
        // Set the root directory from an environment variable
        this.storageRoot = System.getenv("BLOB_STORAGE_ROOT");
        if (this.storageRoot == null) {
            throw new IllegalStateException("BLOB_STORAGE_ROOT environment variable is not set!");
        }

        // Ensure the root directory exists
        try {
            Files.createDirectories(Paths.get(storageRoot));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create storage root directory: " + storageRoot, e);
        }
    }

    /**
     * Helper method to resolve a blob's full path.
     */
    private Path resolveBlobPath(String path) {
        return Paths.get(storageRoot, path.replace("+", "/"));
    }

    /**
     * Writes a blob to the file system.
     *
     * @param path The path (name) of the blob to be stored.
     * @param bytes The byte data of the blob.
     * @return A Result indicating whether the operation succeeded or failed.
     */
    @Override
    public Result<Void> write(String path, byte[] bytes) {
        Path blobPath = resolveBlobPath(path);
        try {
            // Ensure parent directories exist
            Files.createDirectories(blobPath.getParent());
            // Write data to the file
            Files.write(blobPath, bytes);
            return Result.ok();
        } catch (IOException e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Deletes a specific blob from the file system.
     *
     * @param path The path (name) of the blob to be deleted.
     * @return A Result indicating whether the operation succeeded or failed.
     */
    @Override
    public Result<Void> delete(String path) {
        Path blobPath = resolveBlobPath(path);
        try {
            Files.deleteIfExists(blobPath);
            return Result.ok();
        } catch (IOException e) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
    }

    /**
     * Deletes all blobs in a specific path (directory) in the file system.
     *
     * @param path The directory path where blobs should be deleted.
     * @return A Result indicating whether the operation succeeded or failed.
     */
    public Result<Void> deleteAllBlobsInPath(String path) {
        Path directoryPath = resolveBlobPath(path);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath)) {
            for (Path entry : stream) {
                Files.delete(entry);
            }
            return Result.ok();
        } catch (IOException e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Reads a blob's content from the file system.
     *
     * @param path The path (name) of the blob to read.
     * @return A Result containing the byte data of the blob.
     */
    @Override
    public Result<byte[]> read(String path) {
        Path blobPath = resolveBlobPath(path);
        try {
            byte[] data = Files.readAllBytes(blobPath);
            return Result.ok(data);
        } catch (IOException e) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
    }


    /**
     * Reads a blob's content and sends it to the provided sink (consumer).
     *
     * @param path The path (name) of the blob to read.
     * @param sink A Consumer that processes the byte data of the blob.
     * @return A Result indicating whether the operation succeeded or failed.
     */
    @Override
    public Result<Void> read(String path, Consumer<byte[]> sink) {
        Path blobPath = resolveBlobPath(path);
        try {
            byte[] data = Files.readAllBytes(blobPath);
            sink.accept(data);
            return Result.ok();
        } catch (IOException e) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
    }


    /**
     * Lists the names of all blobs in the root directory.
     *
     * @return A list of blob names as strings.
     */
    private List<String> list() {
        List<String> blobNames = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(storageRoot))) {
            for (Path entry : stream) {
                blobNames.add(entry.toString());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to list blobs in storage root: " + storageRoot, e);
        }
        return blobNames;
    }
}