package com.app.prod.exceptions.exceptions;

public class FailedFileUploadException extends RuntimeException {
    public FailedFileUploadException(String message) {
        super(message);
    }
}
