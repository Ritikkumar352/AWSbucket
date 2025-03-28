package com.VidAWS.bucket;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@org.springframework.stereotype.Service
public class S3Service {


    private final S3Client s3client;
    private final S3Presigner s3Presigner;
    private final String bucketName = "ritikkumar352";

    @Autowired
    public S3Service(S3Client s3client,S3Presigner s3Presigner){
        this.s3client = s3client;
        this.s3Presigner = s3Presigner;
    }

    public ResponseEntity<Map<String, String>> upload(MultipartFile file) {
        // unique file name
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueName = originalFileName.replace(extension, "") + "-" + UUID.randomUUID() + extension;
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

            // TODO -> Now save this to PostgreSQL
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


//    public ResponseEntity<Map<String, String>> fetchSingleVideo(String fileName) {
//        GetObjectAclRequest getObjectAclRequest=s3client.getObject()
//
//
//    }

}
