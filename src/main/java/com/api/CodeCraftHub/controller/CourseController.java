package com.api.CodeCraftHub.controller;

import com.api.CodeCraftHub.model.Course;
import com.api.CodeCraftHub.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * HTTP endpoints for creating, viewing, editing, and deleting courses.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<Course> addCourse(@RequestBody Course course) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.addCourse(course));
    }

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/stats")
    public Map<String, Object> getCourseStats() {
        List<Course> courses = courseService.getAllCourses();
        Map<String, Long> coursesByStatus = Map.of(
                "Not Started", countCoursesWithStatus(courses, "Not Started"),
                "In Progress", countCoursesWithStatus(courses, "In Progress"),
                "Completed", countCoursesWithStatus(courses, "Completed"));

        return Map.of(
                "totalCourses", courses.size(),
                "coursesByStatus", coursesByStatus);
    }

    @GetMapping("/{id}")
    public Course getCourse(@PathVariable long id) {
        return courseService.getCourseById(id);
    }

    private long countCoursesWithStatus(List<Course> courses, String status) {
        return courses.stream()
                .filter(course -> status.equals(course.getStatus()))
                .count();
    }

    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable long id, @RequestBody Course course) {
        return courseService.updateCourse(id, course);
    }

    @PatchMapping("/{id}")
    public Course patchCourse(@PathVariable long id, @RequestBody Course courseChanges) {
        return courseService.patchCourse(id, courseChanges);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
