package com.app.prod.storage.file;

import com.app.prod.storage.AbstractStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final AbstractStorage storage;

    private static final Map<FileType, Set<String>> ALLOWED_MIME_TYPES = Map.of(
            FileType.PHOTO, Set.of("image/jpeg", "image/png", "image/gif", "image/webp"),
            FileType.VIDEO, Set.of("video/mp4", "video/webm"),
            FileType.SOUND,  Set.of("audio/mpeg", "audio/wav", "audio/ogg", "audio/aac")
    );

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    public String validateAndSaveFile(MultipartFile file) throws IOException {
        validateFile(file);
        FileType category = getFileCategory(file.getContentType());
        String filePath = storage.uploadFile(file, category);

        log.info("Validated and saved {} file: {}", category, filePath);
        return filePath;
    }

    public String getFileUrl(String filePath) {
        return storage.getPrivateImageUrl(filePath);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File is too large");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isSupportedMimeType(contentType)) {
            log.warn("attempt to upload a file with an unsupported extension {}", contentType);
            throw new IllegalArgumentException("File type " + contentType + " is not supported");
        }
    }

    private boolean isSupportedMimeType(String contentType) {
        return ALLOWED_MIME_TYPES.values().stream()
                .anyMatch(set -> set.contains(contentType));
    }

    private FileType getFileCategory(String contentType) {
        return ALLOWED_MIME_TYPES.entrySet().stream()
                .filter(entry -> entry.getValue().contains(contentType))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("File type " + contentType + " is not supported"));
    }


}
