package com.turggut.sms.service;

import com.turggut.sms.domain.course.Course;
import com.turggut.sms.domain.course.CourseRepository;
import com.turggut.sms.domain.teacher.Teacher;
import com.turggut.sms.domain.teacher.TeacherRepository;
import com.turggut.sms.dto.PageResponse;
import com.turggut.sms.dto.course.CourseRequest;
import com.turggut.sms.dto.course.CourseResponse;
import com.turggut.sms.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public CourseResponse create(CourseRequest request) {
        if (courseRepository.existsByCode(request.code())) {
            throw ApiException.conflict("duplicate_code", "Course code already exists");
        }
        Course course = Course.builder()
                .code(request.code().toUpperCase())
                .name(request.name())
                .description(request.description())
                .credits(request.credits())
                .department(emptyToNull(request.department()))
                .semester(emptyToNull(request.semester()))
                .teacher(resolveTeacher(request.teacherId()))
                .active(request.active() == null ? true : request.active())
                .build();
        course = courseRepository.save(course);
        auditLogService.success("COURSE_CREATE", "Course", course.getId().toString(),
                "code=" + course.getCode());
        return CourseResponse.from(course);
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Course not found"));
        if (!course.getCode().equalsIgnoreCase(request.code())
                && courseRepository.existsByCode(request.code())) {
            throw ApiException.conflict("duplicate_code", "Course code already exists");
        }
        course.setCode(request.code().toUpperCase());
        course.setName(request.name());
        course.setDescription(request.description());
        course.setCredits(request.credits());
        course.setDepartment(emptyToNull(request.department()));
        course.setSemester(emptyToNull(request.semester()));
        course.setTeacher(resolveTeacher(request.teacherId()));
        if (request.active() != null) course.setActive(request.active());
        auditLogService.success("COURSE_UPDATE", "Course", course.getId().toString(), null);
        return CourseResponse.from(course);
    }

    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Course not found"));
        courseRepository.delete(course);
        auditLogService.success("COURSE_DELETE", "Course", id.toString(), "code=" + course.getCode());
    }

    @Transactional
    public CourseResponse assignTeacher(Long courseId, Long teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ApiException.notFound("Course not found"));
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> ApiException.notFound("Teacher not found"));
        course.setTeacher(teacher);
        auditLogService.success("COURSE_ASSIGN_TEACHER", "Course", course.getId().toString(),
                "teacherId=" + teacherId);
        return CourseResponse.from(course);
    }

    @Transactional(readOnly = true)
    public CourseResponse get(Long id) {
        return CourseResponse.from(courseRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Course not found")));
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> search(String q, Pageable pageable) {
        return PageResponse.from(courseRepository.search(q, pageable), CourseResponse::from);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId).stream()
                .map(CourseResponse::from)
                .toList();
    }

    private Teacher resolveTeacher(Long teacherId) {
        if (teacherId == null) return null;
        return teacherRepository.findById(teacherId)
                .orElseThrow(() -> ApiException.badRequest("invalid_teacher", "Teacher not found"));
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
