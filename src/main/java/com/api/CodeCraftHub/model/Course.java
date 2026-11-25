package com.api.CodeCraftHub.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;

/**
 * A course the developer wants to learn.
 *
 * <p>Jackson maps the Java camelCase properties to the snake_case JSON names
 * used by the API.</p>
 */
public class Course {

    private Long id;
    private String name;
    private String description;

    @JsonProperty("target_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    private String status;

    @JsonProperty("created_at")
    private Instant createdAt;

    public Course() {
        // Jackson uses this constructor when reading courses.json.
    }

    public Course(Long id, String name, String description, LocalDate targetDate,
                  String status, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.targetDate = targetDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("target_date")
    public LocalDate getTargetDate() {
        return targetDate;
    }

    @JsonProperty("target_date")
    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("created_at")
    public Instant getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
