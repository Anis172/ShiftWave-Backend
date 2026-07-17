package com.example.restaurantshifthandler.controller;

import com.example.restaurantshifthandler.dto.BreakHistoryResponseDTO;
import com.example.restaurantshifthandler.entity.Restaurant;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.entity.enums.BreakStatus;
import com.example.restaurantshifthandler.entity.enums.BreakType;
import com.example.restaurantshifthandler.mapper.BreakRequestMapper;
import com.example.restaurantshifthandler.security.JwtUtil;
import com.example.restaurantshifthandler.service.BreakRequestService;
import com.example.restaurantshifthandler.service.ShiftService;
import com.example.restaurantshifthandler.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BreakRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class BreakRequestControllerPaginationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private BreakRequestService service;

    @MockitoBean
    private BreakRequestMapper mapper;

    @MockitoBean
    private ShiftService shiftService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private User testUser;
    private Restaurant testRestaurant;
    private BreakHistoryResponseDTO testBreakDTO;

    @BeforeEach
    void setUp() {
        testRestaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .build();

        testUser = User.builder()
                .id(1L)
                .name("Test Manager")
                .email("manager@test.com")
                .restaurant(testRestaurant)
                .build();

        testBreakDTO = BreakHistoryResponseDTO.builder()
                .id(1L)
                .workerName("John Worker")
                .roleName("Line Cook")
                .breakType(BreakType.LUNCH)
                .status(BreakStatus.COMPLETED)
                .requestedAt(LocalDateTime.now().minusHours(2))
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now())
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("manager@test.com", null, List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        when(userService.findByEmail(any())).thenReturn(Optional.of(testUser));
    }

    @Test
    void testGetBreakHistoryPaginated_NoFilters_ReturnsPage() throws Exception {
        Page<BreakHistoryResponseDTO> page = new PageImpl<>(
                List.of(testBreakDTO),
                PageRequest.of(0, 10),
                1
        );

        when(service.findBreakHistory(
                eq(1L), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)
        )).thenReturn(page);

        mockMvc.perform(get("/api/break-requests/history/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].workerName").value("John Worker"))
                .andExpect(jsonPath("$.content[0].roleName").value("Line Cook"))
                .andExpect(jsonPath("$.content[0].breakType").value("LUNCH"))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void testGetBreakHistoryPaginated_WithWorkerNameFilter_ReturnsFilteredPage() throws Exception {
        Page<BreakHistoryResponseDTO> page = new PageImpl<>(
                List.of(testBreakDTO),
                PageRequest.of(0, 10),
                1
        );

        when(service.findBreakHistory(
                eq(1L), eq("John"), isNull(), isNull(), isNull(), eq(0), eq(10)
        )).thenReturn(page);

        mockMvc.perform(get("/api/break-requests/history/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .param("workerName", "John")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].workerName").value("John Worker"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetBreakHistoryPaginated_WithRoleFilter_ReturnsFilteredPage() throws Exception {
        Page<BreakHistoryResponseDTO> page = new PageImpl<>(
                List.of(testBreakDTO),
                PageRequest.of(0, 10),
                1
        );

        when(service.findBreakHistory(
                eq(1L), isNull(), eq(1L), isNull(), isNull(), eq(0), eq(10)
        )).thenReturn(page);

        mockMvc.perform(get("/api/break-requests/history/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .param("roleId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].roleName").value("Line Cook"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetBreakHistoryPaginated_EmptyResult_ReturnsEmptyPage() throws Exception {
        Page<BreakHistoryResponseDTO> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        when(service.findBreakHistory(
                eq(1L), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)
        )).thenReturn(emptyPage);

        mockMvc.perform(get("/api/break-requests/history/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void testGetBreakHistoryPaginated_WithDateFilter_ReturnsFilteredPage() throws Exception {
        Page<BreakHistoryResponseDTO> page = new PageImpl<>(
                List.of(testBreakDTO),
                PageRequest.of(0, 10),
                1
        );

        when(service.findBreakHistory(
                eq(1L), isNull(), isNull(), any(), any(), eq(0), eq(10)
        )).thenReturn(page);

        mockMvc.perform(get("/api/break-requests/history/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].workerName").value("John Worker"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}