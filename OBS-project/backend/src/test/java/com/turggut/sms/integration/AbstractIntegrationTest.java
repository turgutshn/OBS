package com.turggut.sms.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Boots the full Spring context against the in-memory H2 database (profile "test").
 * The context is cached and shared across every integration class, so the bootstrap
 * admin and any created data persist for the whole run — tests use class-scoped
 * identifiers to stay independent.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    private static String cachedAdminToken;

    protected String adminToken() throws Exception {
        if (cachedAdminToken == null) {
            cachedAdminToken = login("admin", "Admin!2345");
        }
        return cachedAdminToken;
    }

    protected String login(String username, String password) throws Exception {
        String body = json.writeValueAsString(Map.of("username", username, "password", password));
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return tree(result).get("accessToken").asText();
    }

    protected JsonNode loginFull(String username, String password) throws Exception {
        String body = json.writeValueAsString(Map.of("username", username, "password", password));
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return tree(result);
    }

    protected MockHttpServletRequestBuilder bearer(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token);
    }

    protected MockHttpServletRequestBuilder jsonBody(MockHttpServletRequestBuilder builder, Object payload) throws Exception {
        return builder.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload));
    }

    protected JsonNode tree(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsString());
    }

    // --- Fixtures (happy-path creation via the public API) ---

    protected JsonNode createStudent(String studentNumber, String email, String password) throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("studentNumber", studentNumber);
        req.put("firstName", "First" + studentNumber);
        req.put("lastName", "Last" + studentNumber);
        req.put("email", email);
        if (password != null) {
            req.put("password", password);
        }
        req.put("department", "Computer Science");
        req.put("enrollmentYear", 2024);
        MvcResult r = mvc.perform(bearer(jsonBody(post("/api/admin/students"), req), adminToken()))
                .andExpect(status().isOk())
                .andReturn();
        return tree(r);
    }

    protected JsonNode createTeacher(String employeeNumber, String email, String password) throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("employeeNumber", employeeNumber);
        req.put("firstName", "Teach" + employeeNumber);
        req.put("lastName", "Er" + employeeNumber);
        req.put("email", email);
        if (password != null) {
            req.put("password", password);
        }
        req.put("title", "Prof");
        req.put("department", "Computer Science");
        MvcResult r = mvc.perform(bearer(jsonBody(post("/api/admin/teachers"), req), adminToken()))
                .andExpect(status().isOk())
                .andReturn();
        return tree(r);
    }

    protected JsonNode createCourse(String code, Long teacherId) throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("code", code);
        req.put("name", "Course " + code);
        req.put("credits", 4);
        req.put("department", "Computer Science");
        req.put("semester", "2024-FALL");
        if (teacherId != null) {
            req.put("teacherId", teacherId);
        }
        MvcResult r = mvc.perform(bearer(jsonBody(post("/api/admin/courses"), req), adminToken()))
                .andExpect(status().isOk())
                .andReturn();
        return tree(r);
    }

    protected JsonNode enroll(Long studentId, Long courseId, String semester) throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("studentId", studentId);
        req.put("courseId", courseId);
        req.put("semester", semester);
        MvcResult r = mvc.perform(bearer(jsonBody(post("/api/admin/enrollments"), req), adminToken()))
                .andExpect(status().isOk())
                .andReturn();
        return tree(r);
    }
}
