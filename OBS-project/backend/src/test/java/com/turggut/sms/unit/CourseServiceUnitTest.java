package com.turggut.sms.unit;

import com.turggut.sms.course.domain.Course;
import com.turggut.sms.course.domain.CourseRepository;
import com.turggut.sms.course.dto.CourseRequest;
import com.turggut.sms.course.dto.CourseResponse;
import com.turggut.sms.course.service.CourseService;
import com.turggut.sms.reporting.service.AuditLogService;
import com.turggut.sms.shared.exception.ApiException;
import com.turggut.sms.teacher.domain.Teacher;
import com.turggut.sms.teacher.domain.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceUnitTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private CourseService courseService;

    private CourseRequest testCourseRequest() {
        return new CourseRequest(
                "CS101",
                "Introduction to Computer Science",
                "Fundamentals of CS",
                3,
                "Computer Science",
                "Fall 2024",
                1L,
                true
        );
    }

    private Course testCourse() {
        return Course.builder()
                .id(1L)
                .code("CS101")
                .name("Introduction to Computer Science")
                .description("Fundamentals of CS")
                .credits(3)
                .department("Computer Science")
                .semester("Fall 2024")
                .teacher(null)
                .active(true)
                .build();
    }

    private Teacher testTeacher() {
        return Teacher.builder()
                .id(1L)
                .employeeNumber("EMP001")
                .firstName("Jane")
                .lastName("Smith")
                .build();
    }

    @Test
    void createCourseSuccessfully() {
        CourseRequest request = testCourseRequest();
        Course course = testCourse();
        Teacher teacher = testTeacher();
        course.setTeacher(teacher);

        when(courseRepository.existsByCode(request.code())).thenReturn(false);
        when(teacherRepository.findById(request.teacherId())).thenReturn(Optional.of(teacher));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.create(request);

        assertNotNull(response);
        assertEquals("CS101", response.code());
        assertEquals("Introduction to Computer Science", response.name());
        verify(auditLogService).success(eq("COURSE_CREATE"), eq("Course"), anyString(), anyString());
    }

    @Test
    void createCourseWithDuplicateCode() {
        CourseRequest request = testCourseRequest();
        when(courseRepository.existsByCode("CS101")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> courseService.create(request));
        assertTrue(exception.getMessage().contains("duplicate"));
    }

    @Test
    void createCourseWithInvalidTeacher() {
        CourseRequest request = testCourseRequest();
        when(courseRepository.existsByCode(request.code())).thenReturn(false);
        when(teacherRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> courseService.create(request));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void updateCourseSuccessfully() {
        Long courseId = 1L;
        CourseRequest request = testCourseRequest();
        Course course = testCourse();
        Teacher teacher = testTeacher();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.existsByCode(request.code())).thenReturn(false);
        when(teacherRepository.findById(request.teacherId())).thenReturn(Optional.of(teacher));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.update(courseId, request);

        assertNotNull(response);
        assertEquals("CS101", response.code());
        verify(auditLogService).success(eq("COURSE_UPDATE"), eq("Course"), anyString(), any());
    }

    @Test
    void updateNonExistentCourse() {
        Long courseId = 999L;
        CourseRequest request = testCourseRequest();

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> courseService.update(courseId, request));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void deleteCourseSuccessfully() {
        Long courseId = 1L;
        Course course = testCourse();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseService.delete(courseId);

        verify(courseRepository).delete(course);
        verify(auditLogService).success(eq("COURSE_DELETE"), eq("Course"), anyString(), anyString());
    }

    @Test
    void deleteNonExistentCourse() {
        Long courseId = 999L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> courseService.delete(courseId));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void getCourseSuccessfully() {
        Long courseId = 1L;
        Course course = testCourse();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        CourseResponse response = courseService.get(courseId);

        assertNotNull(response);
        assertEquals("CS101", response.code());
    }

    @Test
    void getNonExistentCourse() {
        Long courseId = 999L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> courseService.get(courseId));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void assignTeacherToCourseSuccessfully() {
        Long courseId = 1L;
        Long teacherId = 1L;
        Course course = testCourse();
        Teacher teacher = testTeacher();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.assignTeacher(courseId, teacherId);

        assertNotNull(response);
        verify(auditLogService).success(eq("COURSE_ASSIGN_TEACHER"), eq("Course"), anyString(), anyString());
    }

    @Test
    void assignTeacherToNonExistentCourse() {
        Long courseId = 999L;
        Long teacherId = 1L;

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> courseService.assignTeacher(courseId, teacherId));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void searchCoursesSuccessfully() {
        Course course1 = testCourse();
        Course course2 = testCourse();
        course2.setId(2L);
        course2.setCode("CS102");

        Page<Course> page = new PageImpl<>(List.of(course1, course2));
        when(courseRepository.search("CS", Pageable.unpaged())).thenReturn(page);

        com.turggut.sms.shared.dto.PageResponse<CourseResponse> response = courseService.search("CS", Pageable.unpaged());

        assertNotNull(response);
        assertNotNull(response.content());
    }

    @Test
    void findCoursesbyTeacherSuccessfully() {
        Long teacherId = 1L;
        Course course1 = testCourse();
        Course course2 = testCourse();
        course2.setId(2L);
        course2.setCode("CS102");

        when(courseRepository.findByTeacherId(teacherId)).thenReturn(List.of(course1, course2));

        List<CourseResponse> responses = courseService.findByTeacher(teacherId);

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }
}
