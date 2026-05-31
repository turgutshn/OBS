package com.turggut.sms.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void loginSucceedsForAdmin() throws Exception {
        JsonNode body = loginFull("admin", "Admin!2345");
        org.junit.jupiter.api.Assertions.assertNotNull(body.get("accessToken").asText());
        org.junit.jupiter.api.Assertions.assertNotNull(body.get("refreshToken").asText());
        org.junit.jupiter.api.Assertions.assertEquals("ADMIN", body.get("user").get("role").asText());
        org.junit.jupiter.api.Assertions.assertEquals("Administrator", body.get("user").get("fullName").asText());
    }

    @Test
    void loginFailsForUnknownUser() throws Exception {
        mvc.perform(jsonBody(post("/api/auth/login"), Map.of("username", "ghost", "password", "whatever1")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginFailsForWrongPassword() throws Exception {
        createStudent("S-AUTH-1", "auth1@test.local", "Passw0rd!");
        mvc.perform(jsonBody(post("/api/auth/login"), Map.of("username", "s-auth-1", "password", "WRONGpass1")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void repeatedWrongPasswordLocksAccount() throws Exception {
        createStudent("S-LOCK", "lock@test.local", "Passw0rd!");
        for (int i = 0; i < 5; i++) {
            mvc.perform(jsonBody(post("/api/auth/login"), Map.of("username", "s-lock", "password", "bad" + i + "pass")))
                    .andExpect(status().isUnauthorized());
        }
        // Now even the correct password is rejected because the account is locked
        MvcResult result = mvc.perform(jsonBody(post("/api/auth/login"),
                        Map.of("username", "s-lock", "password", "Passw0rd!")))
                .andExpect(status().isUnauthorized())
                .andReturn();
        org.junit.jupiter.api.Assertions.assertTrue(
                tree(result).get("message").asText().toLowerCase().contains("locked"));
    }

    @Test
    void disabledAccountCannotLogin() throws Exception {
        JsonNode student = createStudent("S-DISABLED", "disabled@test.local", "Passw0rd!");
        long id = student.get("id").asLong();
        mvc.perform(bearer(post("/api/admin/students/" + id + "/deactivate"), adminToken()))
                .andExpect(status().isNoContent());
        mvc.perform(jsonBody(post("/api/auth/login"), Map.of("username", "s-disabled", "password", "Passw0rd!")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshRotatesTokenAndReuseRevokesAll() throws Exception {
        createStudent("S-REFRESH", "refresh@test.local", "Passw0rd!");
        JsonNode first = loginFull("s-refresh", "Passw0rd!");
        String refresh = first.get("refreshToken").asText();

        // First refresh works and rotates the token
        MvcResult refreshed = mvc.perform(jsonBody(post("/api/auth/refresh"), Map.of("refreshToken", refresh)))
                .andExpect(status().isOk())
                .andReturn();
        String newRefresh = tree(refreshed).get("refreshToken").asText();
        org.junit.jupiter.api.Assertions.assertNotEquals(refresh, newRefresh);

        // Reusing the now-revoked token is rejected (reuse detection)
        mvc.perform(jsonBody(post("/api/auth/refresh"), Map.of("refreshToken", refresh)))
                .andExpect(status().isUnauthorized());

        // And because reuse revokes ALL tokens, the rotated one is dead too
        mvc.perform(jsonBody(post("/api/auth/refresh"), Map.of("refreshToken", newRefresh)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithUnknownTokenFails() throws Exception {
        mvc.perform(jsonBody(post("/api/auth/refresh"), Map.of("refreshToken", "not-a-real-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        createStudent("S-LOGOUT", "logout@test.local", "Passw0rd!");
        JsonNode session = loginFull("s-logout", "Passw0rd!");
        String refresh = session.get("refreshToken").asText();
        String access = session.get("accessToken").asText();

        mvc.perform(bearer(jsonBody(post("/api/auth/logout"), Map.of("refreshToken", refresh)), access))
                .andExpect(status().isNoContent());
        // Refresh no longer valid
        mvc.perform(jsonBody(post("/api/auth/refresh"), Map.of("refreshToken", refresh)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutWithoutBodyIsNoOp() throws Exception {
        mvc.perform(bearer(post("/api/auth/logout"), adminToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void logoutAllRevokesEverySession() throws Exception {
        createStudent("S-LOGOUTALL", "logoutall@test.local", "Passw0rd!");
        String access = login("s-logoutall", "Passw0rd!");
        JsonNode second = loginFull("s-logoutall", "Passw0rd!");
        String refreshTwo = second.get("refreshToken").asText();

        mvc.perform(bearer(post("/api/auth/logout-all"), access))
                .andExpect(status().isNoContent());
        mvc.perform(jsonBody(post("/api/auth/refresh"), Map.of("refreshToken", refreshTwo)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsCurrentUser() throws Exception {
        mvc.perform(bearer(get("/api/auth/me"), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void meRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePasswordSucceedsThenOldPasswordFails() throws Exception {
        createStudent("S-CHPW", "chpw@test.local", "Passw0rd!");
        String access = login("s-chpw", "Passw0rd!");

        mvc.perform(bearer(jsonBody(post("/api/auth/change-password"),
                        Map.of("currentPassword", "Passw0rd!", "newPassword", "NewPassw0rd!")), access))
                .andExpect(status().isNoContent());

        mvc.perform(jsonBody(post("/api/auth/login"), Map.of("username", "s-chpw", "password", "Passw0rd!")))
                .andExpect(status().isUnauthorized());
        login("s-chpw", "NewPassw0rd!");
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() throws Exception {
        createStudent("S-CHPW2", "chpw2@test.local", "Passw0rd!");
        String access = login("s-chpw2", "Passw0rd!");
        mvc.perform(bearer(jsonBody(post("/api/auth/change-password"),
                        Map.of("currentPassword", "WRONG-current1", "newPassword", "NewPassw0rd!")), access))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePasswordRejectsWeakPassword() throws Exception {
        createStudent("S-CHPW3", "chpw3@test.local", "Passw0rd!");
        String access = login("s-chpw3", "Passw0rd!");
        // No digits -> weak
        mvc.perform(bearer(jsonBody(post("/api/auth/change-password"),
                        Map.of("currentPassword", "Passw0rd!", "newPassword", "onlyletters")), access))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginValidationRejectsBlankUsername() throws Exception {
        mvc.perform(jsonBody(post("/api/auth/login"), Map.of("username", "", "password", "whatever1")))
                .andExpect(status().isBadRequest());
    }
}
