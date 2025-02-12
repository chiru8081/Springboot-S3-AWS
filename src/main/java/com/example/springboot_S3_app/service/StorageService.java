package com.example.springboot_S3_app.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.IoUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private S3Client amazonS3;

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    public String uploadFile(MultipartFile file){

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File fileObj = convertMultiPartFileToFile(file);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            amazonS3.putObject(putObjectRequest, RequestBody.fromFile(fileObj));

            return fileName + " uploaded successfully!";
        } catch (Exception e) {
            log.error("Error occurred while uploading file to S3", e);
            return "File upload failed!";
        } finally {
            fileObj.delete();
        }
    }

    public byte[] downloadFile(String fileName) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            ResponseInputStream<GetObjectResponse> inputStream = amazonS3.getObject(getObjectRequest);


            return IoUtils.toByteArray(inputStream);
        } catch (IOException e) {
            log.error("Error occurred while downloading file: {}", fileName, e);
            return null;
        }
    }

    public String deleteFile(String fileName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            amazonS3.deleteObject(deleteObjectRequest);

            log.info("File {} deleted successfully from S3", fileName);
            return fileName + " removed successfully!";
        } catch (Exception e) {
            log.error("Error deleting file: {}", fileName, e);
            return "Error deleting file: " + fileName;
        }
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try {
            FileOutputStream fos = new FileOutputStream(convertedFile);
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file",e);
        }
        return convertedFile;
    }
}
