package com.turggut.sms.controller;

import com.turggut.sms.dto.PageResponse;
import com.turggut.sms.dto.student.StudentRequest;
import com.turggut.sms.dto.student.StudentResponse;
import com.turggut.sms.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/students")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public PageResponse<StudentResponse> list(@RequestParam(required = false) String q,
                                              @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        return studentService.search(q, pageable);
    }

    @GetMapping("/{id}")
    public StudentResponse get(@PathVariable Long id) {
        return studentService.get(id);
    }

    @PostMapping
    public StudentResponse create(@Valid @RequestBody StudentRequest request) {
        return studentService.create(request);
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return studentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        studentService.setActive(id, true);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        studentService.setActive(id, false);
        return ResponseEntity.noContent().build();
    }
}
