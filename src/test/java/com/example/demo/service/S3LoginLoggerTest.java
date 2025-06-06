package com.example.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for S3LoginLogger.
 *
 * Note: S3LoginLogger calls s3Client.getObject(request, ResponseTransformer.toBytes()).
 * Our test must stub exactly that method signature.
 */
class S3LoginLoggerTest {

    private S3Client mockS3;
    private S3LoginLogger logger;
    private static final String BUCKET = "test-bucket";

    @BeforeEach
    void setup() {
        mockS3 = mock(S3Client.class);
        logger = new S3LoginLogger(mockS3, BUCKET);
    }

    @Test
    void logLogin_appendsToExistingFile() throws IOException {
        // 1) Prepare a fake “existing” file content in S3:
        String existing = "2025-06-01 10:00:00 user1@example.com logged in\n";
        byte[] existingBytes = existing.getBytes(StandardCharsets.UTF_8);

        // Build a fake ResponseBytes<GetObjectResponse> to return
        ResponseBytes<GetObjectResponse> fakeResponse = ResponseBytes.fromByteArray(
                GetObjectResponse.builder().build(),
                existingBytes
        );

        // Stub mockS3.getObject(request, transformer) to return fakeResponse
        when(mockS3.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
                .thenReturn(fakeResponse);

        // 2) Call the method under test:
        logger.logLogin("newuser@example.com");

        // 3) Capture arguments passed to putObject(...)
        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(mockS3).putObject(putCaptor.capture(), bodyCaptor.capture());

        // 4) Verify the S3 key format:
        String key = putCaptor.getValue().key();
        assertTrue(key.startsWith("logins/"), "Expected key to start with 'logins/'");
        assertTrue(key.endsWith(".txt"),  "Expected key to end with '.txt'");

        // 5) Verify the combined content: existing + new entry
        byte[] uploaded = bodyCaptor.getValue()
                .contentStreamProvider()
                .newStream()
                .readAllBytes();
        String combined = new String(uploaded, StandardCharsets.UTF_8);

        assertTrue(combined.startsWith(existing));
        assertTrue(combined.contains("newuser@example.com logged in"));
    }

    @Test
    void logLogin_createsNewFileWhenNotFound() throws IOException {
        // Stub getObject to throw NoSuchKeyException (no existing object)
        when(mockS3.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        // Call method under test
        logger.logLogin("firstuser@example.com");

        // Capture arguments to putObject(...)
        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(mockS3).putObject(putCaptor.capture(), bodyCaptor.capture());

        // Uploaded content should contain only the new entry
        byte[] uploaded = bodyCaptor.getValue()
                .contentStreamProvider()
                .newStream()
                .readAllBytes();
        String content = new String(uploaded, StandardCharsets.UTF_8);
        assertTrue(content.contains("firstuser@example.com logged in"));
    }
}
