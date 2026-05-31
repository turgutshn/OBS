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

class TeacherIntegrationTest extends AbstractIntegrationTest {

    private Map<String, Object> teacherReq(String number, String email) {
        Map<String, Object> req = new HashMap<>();
        req.put("employeeNumber", number);
        req.put("firstName", "Alan");
        req.put("lastName", "Turing");
        req.put("email", email);
        req.put("password", "Passw0rd!");
        req.put("title", "Dr");
        req.put("department", "CS");
        return req;
    }

    @Test
    void createGetUpdateDeleteFlow() throws Exception {
        JsonNode created = createTeacher("T-CRUD", "tcrud@test.local", "Passw0rd!");
        long id = created.get("id").asLong();

        mvc.perform(bearer(get("/api/admin/teachers/" + id), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeNumber").value("T-CRUD"));

        Map<String, Object> update = teacherReq("T-CRUD", "tcrud-updated@test.local");
        update.put("lastName", "Newman");
        mvc.perform(bearer(jsonBody(put("/api/admin/teachers/" + id), update), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Newman"));

        mvc.perform(bearer(delete("/api/admin/teachers/" + id), adminToken()))
                .andExpect(status().isNoContent());
        mvc.perform(bearer(get("/api/admin/teachers/" + id), adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithoutPasswordGeneratesOne() throws Exception {
        Map<String, Object> req = teacherReq("T-NOPW", "tnopw@test.local");
        req.remove("password");
        mvc.perform(bearer(jsonBody(post("/api/admin/teachers"), req), adminToken()))
                .andExpect(status().isOk());
    }

    @Test
    void duplicateEmployeeNumberConflicts() throws Exception {
        createTeacher("T-DUP", "tdup@test.local", "Passw0rd!");
        mvc.perform(bearer(jsonBody(post("/api/admin/teachers"), teacherReq("T-DUP", "tdup2@test.local")), adminToken()))
                .andExpect(status().isConflict());
    }

    @Test
    void duplicateEmailConflicts() throws Exception {
        createTeacher("T-MAIL", "tshared@test.local", "Passw0rd!");
        mvc.perform(bearer(jsonBody(post("/api/admin/teachers"), teacherReq("T-MAIL-2", "tshared@test.local")), adminToken()))
                .andExpect(status().isConflict());
    }

    @Test
    void teacherCanLoginAndFullNameResolves() throws Exception {
        createTeacher("T-LOGIN", "tlogin@test.local", "Passw0rd!");
        JsonNode session = loginFull("t-login", "Passw0rd!");
        org.junit.jupiter.api.Assertions.assertEquals("TEACHER", session.get("user").get("role").asText());
        org.junit.jupiter.api.Assertions.assertTrue(session.get("user").get("fullName").asText().startsWith("TeachT-LOGIN"));
    }

    @Test
    void searchReturnsPage() throws Exception {
        createTeacher("T-SEARCH", "tsearch@test.local", "Passw0rd!");
        mvc.perform(bearer(get("/api/admin/teachers").param("q", "T-SEARCH"), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
