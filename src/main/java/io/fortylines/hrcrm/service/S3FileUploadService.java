package io.fortylines.hrcrm.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface S3FileUploadService {
    void uploadFile(String fileName, File file);
    void upload(String fileName, MultipartFile multipartFile);
    byte[] downloadFile(String fileName);
    void deleteFile(String fileName);
}
