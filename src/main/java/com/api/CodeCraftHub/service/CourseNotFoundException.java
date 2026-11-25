package com.api.CodeCraftHub.service;

public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(long id) {
        super("Course with id " + id + " was not found.");
    }
}
