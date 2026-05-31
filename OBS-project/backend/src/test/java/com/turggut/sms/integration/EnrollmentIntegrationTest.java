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

class EnrollmentIntegrationTest extends AbstractIntegrationTest {

    private long studentId(String number) throws Exception {
        return createStudent(number, number.toLowerCase() + "@test.local", "Passw0rd!").get("id").asLong();
    }

    @Test
    void enrollGradeAndListFlow() throws Exception {
        long sid = studentId("EN-S1");
        long cid = createCourse("EN-C1", null).get("id").asLong();

        JsonNode enrollment = enroll(sid, cid, "2024-FALL");
        long eid = enrollment.get("id").asLong();
        org.junit.jupiter.api.Assertions.assertEquals("ENROLLED", enrollment.get("status").asText());

        // Passing grade -> COMPLETED, letter computed
        mvc.perform(bearer(jsonBody(put("/api/teacher/enrollments/" + eid + "/grade"),
                        Map.of("midtermGrade", 80, "finalGrade", 90)), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.letterGrade").value("BA"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mvc.perform(bearer(get("/api/admin/students/" + sid + "/enrollments"), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("EN-C1"));

        mvc.perform(bearer(get("/api/admin/courses/" + cid + "/enrollments"), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value((int) sid));
    }

    @Test
    void failingFinalGradeMarksFailed() throws Exception {
        long sid = studentId("EN-FAIL");
        long cid = createCourse("EN-CFAIL", null).get("id").asLong();
        long eid = enroll(sid, cid, "2024-FALL").get("id").asLong();

        mvc.perform(bearer(jsonBody(put("/api/teacher/enrollments/" + eid + "/grade"),
                        Map.of("midtermGrade", 10, "finalGrade", 20)), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.letterGrade").value("FF"))
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void duplicateEnrollmentConflicts() throws Exception {
        long sid = studentId("EN-DUP");
        long cid = createCourse("EN-CDUP", null).get("id").asLong();
        enroll(sid, cid, "2024-FALL");
        mvc.perform(bearer(jsonBody(post("/api/admin/enrollments"),
                        Map.of("studentId", sid, "courseId", cid, "semester", "2024-FALL")), adminToken()))
                .andExpect(status().isConflict());
    }

    @Test
    void enrollMissingStudentIs404() throws Exception {
        long cid = createCourse("EN-CNOSTU", null).get("id").asLong();
        mvc.perform(bearer(jsonBody(post("/api/admin/enrollments"),
                        Map.of("studentId", 999999, "courseId", cid, "semester", "2024-FALL")), adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void enrollInInactiveCourseIsBadRequest() throws Exception {
        long sid = studentId("EN-INACT");
        long cid = createCourse("EN-CINACT", null).get("id").asLong();
        // deactivate course
        Map<String, Object> update = new HashMap<>();
        update.put("code", "EN-CINACT");
        update.put("name", "Inactive");
        update.put("credits", 3);
        update.put("active", false);
        mvc.perform(bearer(jsonBody(put("/api/admin/courses/" + cid), update), adminToken()))
                .andExpect(status().isOk());

        mvc.perform(bearer(jsonBody(post("/api/admin/enrollments"),
                        Map.of("studentId", sid, "courseId", cid, "semester", "2024-FALL")), adminToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dropAndDeleteFlow() throws Exception {
        long sid = studentId("EN-DROP");
        long cid = createCourse("EN-CDROP", null).get("id").asLong();
        long eid = enroll(sid, cid, "2024-FALL").get("id").asLong();

        mvc.perform(bearer(post("/api/admin/enrollments/" + eid + "/drop"), adminToken()))
                .andExpect(status().isNoContent());
        mvc.perform(bearer(delete("/api/admin/enrollments/" + eid), adminToken()))
                .andExpect(status().isNoContent());
        mvc.perform(bearer(delete("/api/admin/enrollments/" + eid), adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void gradeMissingEnrollmentIs404() throws Exception {
        mvc.perform(bearer(jsonBody(put("/api/teacher/enrollments/999999/grade"),
                        Map.of("midtermGrade", 50, "finalGrade", 50)), adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentSeesOwnEnrollments() throws Exception {
        long sid = studentId("EN-OWN");
        long cid = createCourse("EN-COWN", null).get("id").asLong();
        enroll(sid, cid, "2024-FALL");
        String token = login("en-own", "Passw0rd!");
        mvc.perform(bearer(get("/api/student/enrollments"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("EN-COWN"));
    }
}
