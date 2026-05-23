package com.turggut.sms.controller;

import com.turggut.sms.dto.PageResponse;
import com.turggut.sms.dto.teacher.TeacherRequest;
import com.turggut.sms.dto.teacher.TeacherResponse;
import com.turggut.sms.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/teachers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public PageResponse<TeacherResponse> list(@RequestParam(required = false) String q,
                                              @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        return teacherService.search(q, pageable);
    }

    @GetMapping("/{id}")
    public TeacherResponse get(@PathVariable Long id) {
        return teacherService.get(id);
    }

    @PostMapping
    public TeacherResponse create(@Valid @RequestBody TeacherRequest request) {
        return teacherService.create(request);
    }

    @PutMapping("/{id}")
    public TeacherResponse update(@PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
        return teacherService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
