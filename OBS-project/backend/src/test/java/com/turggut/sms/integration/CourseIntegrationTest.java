package com.turggut.sms.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseIntegrationTest extends AbstractIntegrationTest {

    private Map<String, Object> courseReq(String code, Long teacherId) {
        Map<String, Object> req = new HashMap<>();
        req.put("code", code);
        req.put("name", "Algorithms " + code);
        req.put("credits", 5);
        req.put("department", "CS");
        req.put("semester", "2024-FALL");
        if (teacherId != null) {
            req.put("teacherId", teacherId);
        }
        return req;
    }

    @Test
    void createGetUpdateDeleteFlow() throws Exception {
        JsonNode created = createCourse("CRS101", null);
        long id = created.get("id").asLong();
        org.junit.jupiter.api.Assertions.assertEquals("CRS101", created.get("code").asText());

        mvc.perform(bearer(get("/api/admin/courses/" + id), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CRS101"));

        Map<String, Object> update = courseReq("CRS101", null);
        update.put("name", "Advanced Algorithms");
        update.put("active", false);
        mvc.perform(bearer(jsonBody(put("/api/admin/courses/" + id), update), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Advanced Algorithms"))
                .andExpect(jsonPath("$.active").value(false));

        mvc.perform(bearer(delete("/api/admin/courses/" + id), adminToken()))
                .andExpect(status().isNoContent());
        mvc.perform(bearer(get("/api/admin/courses/" + id), adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithTeacherAndLowercaseCodeIsUppercased() throws Exception {
        JsonNode teacher = createTeacher("T-CRS", "tcrs@test.local", "Passw0rd!");
        long teacherId = teacher.get("id").asLong();
        mvc.perform(bearer(jsonBody(post("/api/admin/courses"), courseReq("crs202", teacherId)), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CRS202"))
                .andExpect(jsonPath("$.teacherId").value((int) teacherId));
    }

    @Test
    void createWithInvalidTeacherIsBadRequest() throws Exception {
        mvc.perform(bearer(jsonBody(post("/api/admin/courses"), courseReq("CRS-BADT", 999999L)), adminToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateCodeConflicts() throws Exception {
        createCourse("CRS-DUP", null);
        mvc.perform(bearer(jsonBody(post("/api/admin/courses"), courseReq("CRS-DUP", null)), adminToken()))
                .andExpect(status().isConflict());
    }

    @Test
    void assignTeacherUpdatesCourse() throws Exception {
        JsonNode course = createCourse("CRS-ASSIGN", null);
        long courseId = course.get("id").asLong();
        JsonNode teacher = createTeacher("T-ASSIGN", "tassign@test.local", "Passw0rd!");
        long teacherId = teacher.get("id").asLong();

        mvc.perform(bearer(post("/api/admin/courses/" + courseId + "/assign-teacher/" + teacherId), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherId").value((int) teacherId));
    }

    @Test
    void assignTeacherToMissingCourseIs404() throws Exception {
        JsonNode teacher = createTeacher("T-ASSIGN2", "tassign2@test.local", "Passw0rd!");
        long teacherId = teacher.get("id").asLong();
        mvc.perform(bearer(post("/api/admin/courses/999999/assign-teacher/" + teacherId), adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void authenticatedUserCanListCourses() throws Exception {
        createCourse("CRS-LIST", null);
        createStudent("ST-CRSLIST", "stcrslist@test.local", "Passw0rd!");
        String token = login("st-crslist", "Passw0rd!");
        mvc.perform(bearer(get("/api/courses").param("q", "CRS-LIST"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void teacherCoursesEndpoint() throws Exception {
        JsonNode teacher = createTeacher("T-MYCRS", "tmycrs@test.local", "Passw0rd!");
        long teacherId = teacher.get("id").asLong();
        createCourse("CRS-MINE", teacherId);

        // null teacherId -> empty list branch
        mvc.perform(bearer(get("/api/teacher/courses"), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        mvc.perform(bearer(get("/api/teacher/courses").param("teacherId", String.valueOf(teacherId)), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CRS-MINE"));
    }
}
