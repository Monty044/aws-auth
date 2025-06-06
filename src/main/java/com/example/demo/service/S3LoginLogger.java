package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3LoginLogger {

    private static final Logger log = LoggerFactory.getLogger(S3LoginLogger.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3LoginLogger(S3Client s3Client,
                         @Value("${login.log.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public void logLogin(String email) {
        String fileName = LocalDate.now() + ".txt";
        String key = "logins/" + fileName;
        String entry = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + " " + email + " logged in\n";
        try {
            byte[] existing = new byte[0];
            try {
                ResponseBytes<?> resp = s3Client.getObject(GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .build(),
                        ResponseTransformer.toBytes());
                existing = resp.asByteArray();
            } catch (NoSuchKeyException e) {
                // ignore - new file will be created
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(existing);
            out.write(entry.getBytes(StandardCharsets.UTF_8));
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    RequestBody.fromBytes(out.toByteArray()));
        } catch (IOException e) {
            log.warn("Failed to log login to S3", e);
        }
    }
}