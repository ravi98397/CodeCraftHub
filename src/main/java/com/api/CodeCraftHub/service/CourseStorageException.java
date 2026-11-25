package com.api.CodeCraftHub.service;

public class CourseStorageException extends RuntimeException {

    public CourseStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public CourseStorageException(String message) {
        super(message);
    }
}
