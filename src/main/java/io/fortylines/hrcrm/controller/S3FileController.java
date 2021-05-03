package io.fortylines.hrcrm.controller;

import io.fortylines.hrcrm.service.S3FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/files")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
public class S3FileController {

    private final S3FileUploadService s3FileUploadService;

    @Autowired
    public S3FileController(S3FileUploadService s3FileUploadService) {
        this.s3FileUploadService = s3FileUploadService;
    }

    @PostMapping
    public void uploadFile(@RequestParam(value = "file") MultipartFile file,
                           @RequestParam(value = "fileName", required = false) String fileName) {
        s3FileUploadService.upload(fileName, file);
    }

    @DeleteMapping("/{fileName}")
    public void delete(@PathVariable String fileName) {
        s3FileUploadService.deleteFile(fileName);
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String fileName) {
        byte[] data = s3FileUploadService.downloadFile(fileName);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
