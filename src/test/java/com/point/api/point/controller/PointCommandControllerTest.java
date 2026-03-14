package com.point.api.point.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.point.api.point.dto.PointEarnRequest;
import com.point.api.point.dto.PointEarnResponse;
import com.point.api.point.entity.PointTxType;
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
    void 일반_적립요청을_생성한다() throws Exception {
        LocalDateTime expiredAt = LocalDateTime.of(2027, 3, 14, 0, 0, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 14, 10, 0, 0);

        given(pointCommandService.earn(any())).willReturn(new PointEarnResponse(
                1L,
                "point-key-001",
                100L,
                PointTxType.EARN,
                1000L,
                1000L,
                expiredAt,
                createdAt
        ));

        PointEarnRequest request = new PointEarnRequest(100L, 1000L, "point-key-001", false, null, "회원가입 축하");

        mockMvc.perform(post("/api/v1/points/earn")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pointId").value(1L))
                .andExpect(jsonPath("$.data.pointKey").value("point-key-001"))
                .andExpect(jsonPath("$.data.userId").value(100L))
                .andExpect(jsonPath("$.data.txType").value("EARN"))
                .andExpect(jsonPath("$.data.amount").value(1000L))
                .andExpect(jsonPath("$.data.remainedAmount").value(1000L));
    }

    @Test
    void 관리자_수기지급_적립요청을_생성한다() throws Exception {
        LocalDateTime expiredAt = LocalDateTime.of(2027, 3, 14, 0, 0, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 14, 10, 0, 0);

        given(pointCommandService.earn(any())).willReturn(new PointEarnResponse(
                2L,
                "admin-key-001",
                100L,
                PointTxType.ADMIN_GIFT,
                5000L,
                5000L,
                expiredAt,
                createdAt
        ));

        PointEarnRequest request = new PointEarnRequest(100L, 5000L, "admin-key-001", true, 30, "이벤트 수기 지급");

        mockMvc.perform(post("/api/v1/points/earn")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pointId").value(2L))
                .andExpect(jsonPath("$.data.txType").value("ADMIN_GIFT"))
                .andExpect(jsonPath("$.data.amount").value(5000L));
    }

    @Test
    void amount가_0이하면_400을_반환한다() throws Exception {
        PointEarnRequest request = new PointEarnRequest(100L, 0L, "point-key-002", false, null, null);

        mockMvc.perform(post("/api/v1/points/earn")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userId가_null이면_400을_반환한다() throws Exception {
        PointEarnRequest request = new PointEarnRequest(null, 1000L, "point-key-003", false, null, null);

        mockMvc.perform(post("/api/v1/points/earn")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
