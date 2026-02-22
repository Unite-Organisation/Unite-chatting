package com.app.prod.storage.gcp;

import com.app.prod.storage.file.FileType;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CloudStorageService extends GoogleCloudStorage {

    public CloudStorageService(Storage storage) {
        super(storage);
    }

    public String uploadFile(MultipartFile file, FileType category) throws IOException {
        String fileName = prepareFileName(file, category);
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        log.info("File uploaded to Google Cloud Storage: {}", fileName);
        return fileName;
    }

    private String prepareFileName(MultipartFile file, FileType category) {
        return category + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
    }

    @Override
    public String getPrivateImageUrl(String fileName) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                15,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature()
        );

        return signedUrl.toString();
    }
}
