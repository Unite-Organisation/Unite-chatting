package com.app.prod.storage.sandbox;

import com.app.prod.storage.AbstractStorage;
import com.app.prod.storage.file.FileType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/test/image")
@RequiredArgsConstructor
public class ImageTestApi {

    private final AbstractStorage storage;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = storage.uploadFile(file, FileType.PHOTO);
            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error while sending: " + e.getMessage());
        }
    }
}