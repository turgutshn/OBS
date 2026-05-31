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

class StudentIntegrationTest extends AbstractIntegrationTest {

    private Map<String, Object> studentReq(String number, String email) {
        Map<String, Object> req = new HashMap<>();
        req.put("studentNumber", number);
        req.put("firstName", "Ada");
        req.put("lastName", "Lovelace");
        req.put("email", email);
        req.put("password", "Passw0rd!");
        req.put("department", "Math");
        req.put("enrollmentYear", 2023);
        return req;
    }

    @Test
    void createGetUpdateDeleteFlow() throws Exception {
        JsonNode created = createStudent("ST-CRUD", "stcrud@test.local", "Passw0rd!");
        long id = created.get("id").asLong();

        mvc.perform(bearer(get("/api/admin/students/" + id), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentNumber").value("ST-CRUD"))
                .andExpect(jsonPath("$.username").value("st-crud"));

        Map<String, Object> update = studentReq("ST-CRUD", "stcrud-updated@test.local");
        update.put("firstName", "Grace");
        mvc.perform(bearer(jsonBody(put("/api/admin/students/" + id), update), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Grace"))
                .andExpect(jsonPath("$.email").value("stcrud-updated@test.local"));

        mvc.perform(bearer(delete("/api/admin/students/" + id), adminToken()))
                .andExpect(status().isNoContent());

        mvc.perform(bearer(get("/api/admin/students/" + id), adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithoutPasswordGeneratesOne() throws Exception {
        Map<String, Object> req = studentReq("ST-NOPW", "stnopw@test.local");
        req.remove("password");
        mvc.perform(bearer(jsonBody(post("/api/admin/students"), req), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentNumber").value("ST-NOPW"));
    }

    @Test
    void duplicateStudentNumberConflicts() throws Exception {
        createStudent("ST-DUP", "stdup@test.local", "Passw0rd!");
        mvc.perform(bearer(jsonBody(post("/api/admin/students"), studentReq("ST-DUP", "stdup2@test.local")), adminToken()))
                .andExpect(status().isConflict());
    }

    @Test
    void duplicateEmailConflicts() throws Exception {
        createStudent("ST-DUPMAIL", "shared@test.local", "Passw0rd!");
        mvc.perform(bearer(jsonBody(post("/api/admin/students"), studentReq("ST-DUPMAIL-2", "shared@test.local")), adminToken()))
                .andExpect(status().isConflict());
    }

    @Test
    void activateDeactivateTogglesStatus() throws Exception {
        JsonNode created = createStudent("ST-TOGGLE", "sttoggle@test.local", "Passw0rd!");
        long id = created.get("id").asLong();
        mvc.perform(bearer(post("/api/admin/students/" + id + "/deactivate"), adminToken()))
                .andExpect(status().isNoContent());
        mvc.perform(bearer(get("/api/admin/students/" + id), adminToken()))
                .andExpect(jsonPath("$.active").value(false));
        mvc.perform(bearer(post("/api/admin/students/" + id + "/activate"), adminToken()))
                .andExpect(status().isNoContent());
        mvc.perform(bearer(get("/api/admin/students/" + id), adminToken()))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void searchReturnsPage() throws Exception {
        createStudent("ST-SEARCH", "stsearch@test.local", "Passw0rd!");
        mvc.perform(bearer(get("/api/admin/students").param("q", "ST-SEARCH"), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    void getMissingStudentReturns404() throws Exception {
        mvc.perform(bearer(get("/api/admin/students/999999"), adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidPayloadReturns400() throws Exception {
        Map<String, Object> req = studentReq("ST-BAD", "not-an-email");
        mvc.perform(bearer(jsonBody(post("/api/admin/students"), req), adminToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void studentCannotAccessAdminEndpoint() throws Exception {
        createStudent("ST-FORBID", "stforbid@test.local", "Passw0rd!");
        String token = login("st-forbid", "Passw0rd!");
        mvc.perform(bearer(get("/api/admin/students"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousIsUnauthorized() throws Exception {
        mvc.perform(get("/api/admin/students"))
                .andExpect(status().isUnauthorized());
    }
}
