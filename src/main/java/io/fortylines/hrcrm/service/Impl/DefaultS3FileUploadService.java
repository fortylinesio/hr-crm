package io.fortylines.hrcrm.service.Impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import io.fortylines.hrcrm.service.S3FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class DefaultS3FileUploadService implements S3FileUploadService {

    @Value("${s3.bucket.name}")
    private String defaultBucketName;

    private final AmazonS3 amazonS3;

    @Autowired
    public DefaultS3FileUploadService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public void upload(String fileName, MultipartFile file) {
        File fileObj = convertMultiPartFileToFile(file);
        if (fileName == null) {
            String dateNow = LocalDateTime.now().withNano(0).withSecond(0).toString().replaceAll(":", "-");
            String name = dateNow + "_" + file.getOriginalFilename();
            amazonS3.putObject(new PutObjectRequest(defaultBucketName, name, fileObj));
        } else
            amazonS3.putObject(new PutObjectRequest(defaultBucketName, fileName, fileObj));
        fileObj.delete();
    }

    @Override
    public void uploadFile(String fileName, File file) {
        amazonS3.putObject(new PutObjectRequest(defaultBucketName, fileName, file));
    }

    @Override
    public byte[] downloadFile(String fileName) {
        S3Object s3Object = amazonS3.getObject(defaultBucketName, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteFile(String fileName) {
        amazonS3.deleteObject(defaultBucketName, fileName);
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return convertedFile;
    }
}
