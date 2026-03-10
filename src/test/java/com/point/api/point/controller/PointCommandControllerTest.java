package com.point.api.point.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.point.api.point.dto.PointTransactionResponse;
import com.point.api.point.entity.PointTransactionType;
import com.point.api.point.service.PointCommandService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointCommandController.class)
class PointCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PointCommandService pointCommandService;

    @Test
    void 적립요청을_생성한다() throws Exception {
        given(pointCommandService.accrue(any())).willReturn(new PointTransactionResponse(
                1L,
                "member-001",
                1000L,
                PointTransactionType.ACCRUAL,
                "order-001",
                LocalDateTime.of(2026, 3, 10, 17, 40, 0)
        ));

        mockMvc.perform(post("/api/v1/points/accruals")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RequestFixture())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(1L))
                .andExpect(jsonPath("$.memberId").value("member-001"))
                .andExpect(jsonPath("$.amount").value(1000L))
                .andExpect(jsonPath("$.transactionType").value("ACCRUAL"))
                .andExpect(jsonPath("$.orderId").value("order-001"));
    }

    private record RequestFixture(
            String memberId,
            long amount,
            String orderId,
            String description
    ) {
        private RequestFixture() {
            this("member-001", 1000L, "order-001", "signup bonus");
        }
    }
}
