package com.example.issuespot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.endpoint}")
    private String endpointUrl;

    public List<String> uploadFiles(List<MultipartFile> files) {

        List<String> fileUrls = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            return fileUrls;
        }

        for (MultipartFile file : files) {

            if (file.isEmpty()) {
                continue;
            }

            try {

                String originalFilename =
                        file.getOriginalFilename() != null
                                ? file.getOriginalFilename().replaceAll("\\s+", "_")
                                : "file";

                String fileName =
                        UUID.randomUUID().toString().replace("-", "")
                                + "_"
                                + originalFilename;

                byte[] bytes = file.getBytes();

                PutObjectRequest request =
                        PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(fileName)
                                .contentType(file.getContentType())
                                .build();

                s3Client.putObject(
                        request,
                        RequestBody.fromBytes(bytes)
                );

                String baseUrl =
                        endpointUrl.replace("/s3", "/object/public");

                String publicUrl =
                        baseUrl
                                + "/"
                                + bucketName
                                + "/"
                                + fileName;

                fileUrls.add(publicUrl);

            } catch (IOException e) {

                log.error("Failed to upload file", e);

                throw new RuntimeException(
                        "Failed to upload file: " + e.getMessage()
                );
            }
        }

        return fileUrls;
    }
}