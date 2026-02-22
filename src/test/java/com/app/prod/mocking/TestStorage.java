package com.app.prod.mocking;

import com.app.prod.storage.AbstractStorage;
import com.app.prod.storage.file.FileType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class TestStorage implements AbstractStorage {

    @Override
    public String uploadFile(MultipartFile file, FileType category) throws IOException {
        return "test-filepath";
    }

    @Override
    public String getPrivateImageUrl(String fileName) {
        return "test-filepath-tokenized";
    }
}
