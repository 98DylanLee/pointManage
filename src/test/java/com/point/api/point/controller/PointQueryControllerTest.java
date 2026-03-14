package com.point.api.point.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.point.api.point.dto.PointHistoryItemResponse;
import com.point.api.point.dto.PointHistoryResponse;
import com.point.api.point.entity.PointTxType;
import com.point.api.point.service.PointQueryService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointQueryController.class)
class PointQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointQueryService pointQueryService;

    @Test
    void 기간내_포인트_히스토리를_조회한다() throws Exception {
        given(pointQueryService.getHistory(100L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                .willReturn(new PointHistoryResponse(
                        100L,
                        LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 3, 31),
                        List.of(
                                new PointHistoryItemResponse(
                                        1L,
                                        "A",
                                        PointTxType.EARN,
                                        1000L,
                                        0L,
                                        null,
                                        null,
                                        LocalDateTime.of(2027, 3, 1, 0, 0),
                                        LocalDateTime.of(2026, 3, 1, 10, 0)
                                ),
                                new PointHistoryItemResponse(
                                        2L,
                                        "C",
                                        PointTxType.USE,
                                        1000L,
                                        0L,
                                        "ORDER-1",
                                        null,
                                        null,
                                        LocalDateTime.of(2026, 3, 2, 11, 0)
                                )
                        )
                ));

        mockMvc.perform(get("/api/v1/points/history")
                        .param("userId", "100")
                        .param("startDate", "2026-03-01")
                        .param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(100L))
                .andExpect(jsonPath("$.data.histories[0].pointKey").value("A"))
                .andExpect(jsonPath("$.data.histories[1].orderNo").value("ORDER-1"));
    }
}
