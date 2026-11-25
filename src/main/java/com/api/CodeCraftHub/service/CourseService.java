package com.api.CodeCraftHub.service;

import com.api.CodeCraftHub.model.Course;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Reads and writes courses in a JSON file instead of using a database.
 */
@Service
public class CourseService {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("Not Started", "In Progress", "Completed");
    private static final TypeReference<List<Course>> COURSE_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final Path coursesFile;

    public CourseService(ObjectMapper objectMapper,
                         @Value("${app.courses-file:courses.json}") String coursesFile) {
        this.objectMapper = objectMapper;
        this.coursesFile = Path.of(coursesFile);
    }

    /**
     * Create the file the first time the application starts.
     */
    @PostConstruct
    public void initializeFile() {
        try {
            Path parent = coursesFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (Files.notExists(coursesFile)) {
                writeCourses(List.of());
            }
        } catch (IOException exception) {
            throw new CourseStorageException("Could not create or initialize courses.json.", exception);
        }
    }

    public synchronized List<Course> getAllCourses() {
        return new ArrayList<>(readCourses());
    }

    public synchronized Course getCourseById(long id) {
        return findCourse(readCourses(), id)
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    public synchronized Course addCourse(Course course) {
        validateCourse(course);

        List<Course> courses = readCourses();
        long nextId = courses.stream()
                .mapToLong(existing -> existing.getId() == null ? 0 : existing.getId())
                .max()
                .orElse(0) + 1;

        // Never trust server-managed values supplied in the request body.
        Course savedCourse = new Course(nextId, course.getName(), course.getDescription(),
                course.getTargetDate(), course.getStatus(), Instant.now());
        courses.add(savedCourse);
        writeCourses(courses);
        return savedCourse;
    }

    public synchronized Course updateCourse(long id, Course updatedCourse) {
        validateCourse(updatedCourse);

        List<Course> courses = readCourses();
        int index = indexOfCourse(courses, id);
        if (index < 0) {
            throw new CourseNotFoundException(id);
        }

        Course existingCourse = courses.get(index);
        Course savedCourse = new Course(id, updatedCourse.getName(),
                updatedCourse.getDescription(), updatedCourse.getTargetDate(),
                updatedCourse.getStatus(), existingCourse.getCreatedAt());
        courses.set(index, savedCourse);
        writeCourses(courses);
        return savedCourse;
    }

    public synchronized Course patchCourse(long id, Course courseChanges) {
        if (courseChanges == null) {
            throw new InvalidCourseException("Request body must contain at least one field to update.");
        }
        if (courseChanges.getName() == null
                && courseChanges.getDescription() == null
                && courseChanges.getTargetDate() == null
                && courseChanges.getStatus() == null) {
            throw new InvalidCourseException("Request body must contain at least one field to update.");
        }

        List<Course> courses = readCourses();
        int index = indexOfCourse(courses, id);
        if (index < 0) {
            throw new CourseNotFoundException(id);
        }

        Course existingCourse = courses.get(index);
        Course updatedCourse = new Course(
                id,
                courseChanges.getName() != null ? courseChanges.getName() : existingCourse.getName(),
                courseChanges.getDescription() != null
                        ? courseChanges.getDescription() : existingCourse.getDescription(),
                courseChanges.getTargetDate() != null
                        ? courseChanges.getTargetDate() : existingCourse.getTargetDate(),
                courseChanges.getStatus() != null ? courseChanges.getStatus() : existingCourse.getStatus(),
                existingCourse.getCreatedAt());

        validateCourse(updatedCourse);
        courses.set(index, updatedCourse);
        writeCourses(courses);
        return updatedCourse;
    }

    public synchronized void deleteCourse(long id) {
        List<Course> courses = readCourses();
        boolean removed = courses.removeIf(course -> course.getId() != null && course.getId() == id);
        if (!removed) {
            throw new CourseNotFoundException(id);
        }
        writeCourses(courses);
    }

    private void validateCourse(Course course) {
        if (course == null
                || isBlank(course.getName())
                || isBlank(course.getDescription())
                || course.getTargetDate() == null
                || isBlank(course.getStatus())) {
            throw new InvalidCourseException(
                    "Required fields are name, description, target_date (YYYY-MM-DD), and status.");
        }
        if (!ALLOWED_STATUSES.contains(course.getStatus())) {
            throw new InvalidCourseException(
                    "Invalid status. Use exactly: Not Started, In Progress, or Completed.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private List<Course> readCourses() {
        String json;
        try {
            json = Files.readString(coursesFile);
        } catch (IOException exception) {
            throw new CourseStorageException("Could not read courses.json.", exception);
        }

        try {
            List<Course> courses = objectMapper.readValue(json, COURSE_LIST_TYPE);
            if (courses == null) {
                throw new CourseStorageException("courses.json must contain a JSON array.");
            }
            return courses;
        } catch (JacksonException exception) {
            throw new CourseStorageException("courses.json contains invalid JSON.", exception);
        }
    }

    private void writeCourses(List<Course> courses) {
        Path temporaryFile = coursesFile.resolveSibling(coursesFile.getFileName() + ".tmp");
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(temporaryFile.toFile(), courses);
            try {
                Files.move(temporaryFile, coursesFile,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                // Some file systems do not support atomic moves; replacement still works there.
                Files.move(temporaryFile, coursesFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (JacksonException | IOException exception) {
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw new CourseStorageException("Could not write courses.json.", exception);
        }
    }

    private Optional<Course> findCourse(List<Course> courses, long id) {
        return courses.stream()
                .filter(course -> course.getId() != null && course.getId() == id)
                .findFirst();
    }

    private int indexOfCourse(List<Course> courses, long id) {
        for (int index = 0; index < courses.size(); index++) {
            Course course = courses.get(index);
            if (course.getId() != null && course.getId() == id) {
                return index;
            }
        }
        return -1;
    }
}
