package com.turggut.sms.integration;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportIntegrationTest extends AbstractIntegrationTest {

    @Test
    void summaryReturnsAggregates() throws Exception {
        // ensure there is some data
        long sid = createStudent("RPT-S", "rpts@test.local", "Passw0rd!").get("id").asLong();
        long cid = createCourse("RPT-C", null).get("id").asLong();
        enroll(sid, cid, "2024-FALL");

        mvc.perform(bearer(get("/api/admin/reports/summary"), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").isNumber())
                .andExpect(jsonPath("$.totalCourses").isNumber())
                .andExpect(jsonPath("$.studentsByDepartment").exists())
                .andExpect(jsonPath("$.feesByStatus").exists());
    }

    @Test
    void transcriptGeneratesWithGradesAndGpa() throws Exception {
        long sid = createStudent("RPT-TR", "rpttr@test.local", "Passw0rd!").get("id").asLong();
        long c1 = createCourse("RPT-TR1", null).get("id").asLong();
        long c2 = createCourse("RPT-TR2", null).get("id").asLong();
        long e1 = enroll(sid, c1, "2024-FALL").get("id").asLong();
        long e2 = enroll(sid, c2, "2024-SPRING").get("id").asLong();

        mvc.perform(bearer(jsonBody(put("/api/teacher/enrollments/" + e1 + "/grade"),
                Map.of("midtermGrade", 90, "finalGrade", 90)), adminToken())).andExpect(status().isOk());
        mvc.perform(bearer(jsonBody(put("/api/teacher/enrollments/" + e2 + "/grade"),
                Map.of("midtermGrade", 60, "finalGrade", 70)), adminToken())).andExpect(status().isOk());

        mvc.perform(bearer(get("/api/admin/reports/transcript/" + sid), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value((int) sid))
                .andExpect(jsonPath("$.cumulativeGpa").isNumber())
                .andExpect(jsonPath("$.semesters").isArray())
                .andExpect(jsonPath("$.totalCredits").isNumber());
    }

    @Test
    void transcriptForMissingStudentIs404() throws Exception {
        mvc.perform(bearer(get("/api/admin/reports/transcript/999999"), adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentSeesOwnTranscript() throws Exception {
        long sid = createStudent("RPT-OWN", "rptown@test.local", "Passw0rd!").get("id").asLong();
        long cid = createCourse("RPT-OWNC", null).get("id").asLong();
        enroll(sid, cid, "2024-FALL");
        String token = login("rpt-own", "Passw0rd!");
        mvc.perform(bearer(get("/api/student/transcript"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentNumber").value("RPT-OWN"));
    }

    @Test
    void auditLogsAreSearchable() throws Exception {
        createStudent("RPT-AUDIT", "rptaudit@test.local", "Passw0rd!");
        mvc.perform(bearer(get("/api/admin/audit-logs"), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        mvc.perform(bearer(get("/api/admin/audit-logs").param("action", "STUDENT_CREATE"), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        mvc.perform(bearer(get("/api/admin/audit-logs").param("actor", "admin"), adminToken()))
                .andExpect(status().isOk());
    }
}
