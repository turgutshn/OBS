package com.turggut.sms.controller;

import com.turggut.sms.dto.PageResponse;
import com.turggut.sms.dto.course.CourseRequest;
import com.turggut.sms.dto.course.CourseResponse;
import com.turggut.sms.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/admin/courses")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<CourseResponse> adminList(@RequestParam(required = false) String q,
                                                  @PageableDefault(size = 20, sort = "code") Pageable pageable) {
        return courseService.search(q, pageable);
    }

    @GetMapping("/admin/courses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CourseResponse adminGet(@PathVariable Long id) {
        return courseService.get(id);
    }

    @PostMapping("/admin/courses")
    @PreAuthorize("hasRole('ADMIN')")
    public CourseResponse create(@Valid @RequestBody CourseRequest request) {
        return courseService.create(request);
    }

    @PutMapping("/admin/courses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return courseService.update(id, request);
    }

    @DeleteMapping("/admin/courses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/courses/{id}/assign-teacher/{teacherId}")
    @PreAuthorize("hasRole('ADMIN')")
    public CourseResponse assignTeacher(@PathVariable Long id, @PathVariable Long teacherId) {
        return courseService.assignTeacher(id, teacherId);
    }

    @GetMapping("/courses")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<CourseResponse> list(@RequestParam(required = false) String q,
                                             @PageableDefault(size = 20, sort = "code") Pageable pageable) {
        return courseService.search(q, pageable);
    }

    @GetMapping("/teacher/courses")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<CourseResponse> myCourses(@RequestParam(required = false) Long teacherId) {
        if (teacherId == null) {
            return List.of();
        }
        return courseService.findByTeacher(teacherId);
    }
}
