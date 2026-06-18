package com.example.realtimechat.analytics.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.realtimechat.analytics.api.dto.MessagesPerMinuteResponse;
import com.example.realtimechat.analytics.application.AnalyticsService;
import com.example.realtimechat.auth.security.JpaUserDetailsService;
import com.example.realtimechat.auth.security.JwtAuthenticationFilter;
import com.example.realtimechat.auth.security.JwtService;
import com.example.realtimechat.config.StaticResourceConfig;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.FilterType;

@WebMvcTest(
        controllers = AnalyticsController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, StaticResourceConfig.class}
        )
)
@AutoConfigureMockMvc(addFilters = false)
@Import(AnalyticsControllerTest.MethodSecurityTestConfig.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JpaUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void messagesPerMinuteRequiresAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/messages-per-minute"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void messagesPerMinuteReturnsAnalyticsForAdmin() throws Exception {
        when(analyticsService.messagesPerMinute(10)).thenReturn(List.of(new MessagesPerMinuteResponse(
                Instant.parse("2026-06-18T01:00:00Z"),
                Instant.parse("2026-06-18T01:01:00Z"),
                42
        )));

        mockMvc.perform(get("/api/v1/analytics/messages-per-minute")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].messageCount").value(42));
    }

    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }
}
