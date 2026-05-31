package com.turggut.sms.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeeIntegrationTest extends AbstractIntegrationTest {

    private long studentId(String number) throws Exception {
        return createStudent(number, number.toLowerCase() + "@test.local", "Passw0rd!").get("id").asLong();
    }

    private Map<String, Object> feeReq(long studentId, String semester, String amount) {
        Map<String, Object> req = new HashMap<>();
        req.put("studentId", studentId);
        req.put("semester", semester);
        req.put("amount", amount);
        req.put("dueDate", LocalDate.now().plusDays(30).toString());
        req.put("description", "Tuition");
        return req;
    }

    @Test
    void assessUpdateAndPayInFull() throws Exception {
        long sid = studentId("FEE-PAY");
        JsonNode fee = postFee(feeReq(sid, "2024-FALL", "1000.00"));
        long feeId = fee.get("id").asLong();
        org.junit.jupiter.api.Assertions.assertEquals("PENDING", fee.get("status").asText());

        // re-assess (same student+semester) updates the existing fee
        JsonNode reassessed = postFee(feeReq(sid, "2024-FALL", "1200.00"));
        org.junit.jupiter.api.Assertions.assertEquals(feeId, reassessed.get("id").asLong());

        // partial payment
        mvc.perform(bearer(jsonBody(post("/api/admin/fees/" + feeId + "/payments"), Map.of("amount", "200.00")), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIAL"));

        // pay remaining -> PAID
        mvc.perform(bearer(jsonBody(post("/api/admin/fees/" + feeId + "/payments"), Map.of("amount", "1000.00")), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.outstanding").value(0));
    }

    @Test
    void overpaymentIsRejected() throws Exception {
        long sid = studentId("FEE-OVER");
        long feeId = postFee(feeReq(sid, "2024-FALL", "500.00")).get("id").asLong();
        mvc.perform(bearer(jsonBody(post("/api/admin/fees/" + feeId + "/payments"), Map.of("amount", "600.00")), adminToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void waivedFeeCannotBePaid() throws Exception {
        long sid = studentId("FEE-WAIVE");
        long feeId = postFee(feeReq(sid, "2024-FALL", "500.00")).get("id").asLong();
        mvc.perform(bearer(post("/api/admin/fees/" + feeId + "/waive"), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAIVED"));
        mvc.perform(bearer(jsonBody(post("/api/admin/fees/" + feeId + "/payments"), Map.of("amount", "100.00")), adminToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listFilterByStatusAndStudent() throws Exception {
        long sid = studentId("FEE-LIST");
        postFee(feeReq(sid, "2024-FALL", "300.00"));
        mvc.perform(bearer(get("/api/admin/fees"), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        mvc.perform(bearer(get("/api/admin/fees").param("status", "PENDING"), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        mvc.perform(bearer(get("/api/admin/students/" + sid + "/fees"), adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].semester").value("2024-FALL"));
    }

    @Test
    void deleteFeeFlow() throws Exception {
        long sid = studentId("FEE-DEL");
        long feeId = postFee(feeReq(sid, "2024-FALL", "300.00")).get("id").asLong();
        mvc.perform(bearer(delete("/api/admin/fees/" + feeId), adminToken()))
                .andExpect(status().isNoContent());
        mvc.perform(bearer(delete("/api/admin/fees/" + feeId), adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void payMissingFeeIs404() throws Exception {
        mvc.perform(bearer(jsonBody(post("/api/admin/fees/999999/payments"), Map.of("amount", "10.00")), adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentSeesOwnFees() throws Exception {
        long sid = studentId("FEE-OWN");
        postFee(feeReq(sid, "2024-FALL", "750.00"));
        String token = login("fee-own", "Passw0rd!");
        mvc.perform(bearer(get("/api/student/fees"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(750.00));
    }

    private JsonNode postFee(Map<String, Object> req) throws Exception {
        return tree(mvc.perform(bearer(jsonBody(post("/api/admin/fees"), req), adminToken()))
                .andExpect(status().isOk())
                .andReturn());
    }
}
