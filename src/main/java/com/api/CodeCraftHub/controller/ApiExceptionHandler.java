package com.api.CodeCraftHub.controller;

import com.api.CodeCraftHub.service.CourseNotFoundException;
import com.api.CodeCraftHub.service.CourseStorageException;
import com.api.CodeCraftHub.service.InvalidCourseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Converts expected application errors into clear JSON HTTP responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidCourseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidCourse(InvalidCourseException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(CourseNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleCourseNotFound(CourseNotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUnreadableRequest(HttpMessageNotReadableException exception) {
        return Map.of("error", "Request body is invalid. Check required fields and use target_date format YYYY-MM-DD.");
    }

    @ExceptionHandler(CourseStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleStorageError(CourseStorageException exception) {
        return Map.of("error", exception.getMessage());
    }
}
