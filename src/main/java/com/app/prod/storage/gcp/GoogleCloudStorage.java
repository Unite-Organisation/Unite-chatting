package com.app.prod.storage.gcp;

import com.app.prod.storage.AbstractStorage;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;

public abstract class GoogleCloudStorage implements AbstractStorage {

    protected final String GOOGLE_API_LINK = "https://storage.googleapis.com/%s/%s";
    protected final Storage storage;

    @Value("${gcp.bucket.name}")
    protected String bucketName;

    protected GoogleCloudStorage(Storage storage) {
        this.storage = storage;
    }
}
