package com.VidAWS.bucket;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class S3Service {


    private final S3Client s3client;
    private final S3Presigner s3Presigner;
    private final videoRepo videoRepo;
    private final String bucketName = "ritikkumar352";

    @Autowired
    public S3Service(S3Client s3client, S3Presigner s3Presigner, videoRepo videoRepo) {
        this.s3client = s3client;
        this.s3Presigner = s3Presigner;
        this.videoRepo = videoRepo;
    }

    public ResponseEntity<Map<String, String>> upload(MultipartFile file) {
        // unique file name
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueName = originalFileName.replace(extension, "") + "-" + UUID.randomUUID() + extension;  // key ==> uniqueName
        try {
            // convert multipart to AWS SDK RequestBody-> inputStream needed for s3 upload... and needs exact file size before upload
            RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(), file.getSize());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .contentType(file.getContentType())
                    .key(uniqueName)  // also use for file path
                    .build(); // acl(object... file private -> access only using pre-signed URL

            // Now upload and
            PutObjectResponse response = s3client
                    .putObject(putObjectRequest, requestBody);  // this response has etag and status code

            Map<String, String> result = new HashMap<>();
            result.put("message", "Upload successful");
            result.put("etag", response.eTag()); //?

            System.out.println(response.sdkHttpResponse().headers() + " <-- header");
            System.out.println("Upload done.. IG");

            String presignedViewUrl = generatePresignedViewUrl(uniqueName);
            String presignedDownloadUrl = generateDownloadUrl(uniqueName);

            // TODO -> Now save this to PostgreSQL
            boolean DbRes = save(presignedViewUrl, presignedDownloadUrl, file);
            if (!DbRes) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Failed to save metadata in DB");
                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);

//            return "https://" + bucketName + ".s3.amazonaws.com/" + uniqueName;  // -> url structure for public file

//            return new ResponseEntity<>(result,
//                    HttpStatus.valueOf(response.sdkHttpResponse().statusCode()));
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to upload video");
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // TODO -> first save to postgreSQL then get from db and get a presigned url

    public boolean save(String presignedViewUrl, String presignedDownloadUrl, MultipartFile file) {
        Video video = new Video();

        video.setViewUrl(presignedViewUrl);
        video.setDownloadUrl(presignedDownloadUrl);

        video.setUploadedAt(LocalDateTime.now()); // prepersist not working

        double fileSizeInMB = file.getSize() / (1024.0 * 1024.0);
        video.setFileSize((double) Math.round(fileSizeInMB * 100.0) / 100.0); // in MB ...-> round of

        video.setContentType(file.getContentType());
        video.setTitle(file.getOriginalFilename());  // orignal file name or set key ??
        Video res = videoRepo.save(video);
        System.out.println(res);
        return res.getId() > 0;
    }


//    public ResponseEntity<Map<String, String>> fetchSingleVideo(String fileName) {
//        GetObjectAclRequest getObjectAclRequest=s3client.getObject()
//
//
//    }


    public String generatePresignedViewUrl(String uniqueName) {
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofHours(3))
                        .getObjectRequest(r -> r.bucket(bucketName).key(uniqueName))
                        .build()
        );
        return presignedRequest.url().toString();
    }

    private String generateDownloadUrl(String uniqueName) {
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofHours(2))
                        .getObjectRequest(r -> r
                                .bucket(bucketName)
                                .key(uniqueName)
                                .responseContentDisposition("attachment") // Forces download
                        )
                        .build()
        );
        return presignedRequest.url().toString();
    }

}
