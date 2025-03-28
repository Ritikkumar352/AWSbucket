package com.VidAWS.bucket;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;


@Configuration
public class S3Config {

    private final Dotenv dotenv = Dotenv.load();

    @Bean
    public software.amazon.awssdk.services.s3.S3Client getS3Client() {

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                dotenv.get("AWS_ACCESS_KEY_ID"),
                dotenv.get("AWS_SECRET_ACCESS_KEY")
        );

        return software.amazon.awssdk.services.s3.S3Client.builder()
                .region(Region.of(dotenv.get("AWS_REGION")))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(){

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                dotenv.get("AWS_ACCESS_KEY_ID"),
                dotenv.get("AWS_SECRET_ACCESS_KEY")
        );

        return S3Presigner.builder()
                .region(Region.of(dotenv.get("AWS_REGION")))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }


}

