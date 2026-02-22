package com.app.prod.storage;

import com.app.prod.storage.file.FileType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AbstractStorage {
    String uploadFile(MultipartFile file, FileType category) throws IOException;
    String getPrivateImageUrl(String fileName);
}
