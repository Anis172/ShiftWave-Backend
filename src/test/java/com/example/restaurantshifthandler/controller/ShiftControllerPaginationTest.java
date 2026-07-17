package com.example.restaurantshifthandler.controller;

import com.example.restaurantshifthandler.dto.ShiftResponseDTO;
import com.example.restaurantshifthandler.entity.Restaurant;
import com.example.restaurantshifthandler.entity.Role;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.entity.enums.ShiftStatus;
import com.example.restaurantshifthandler.security.JwtUtil;
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

@WebMvcTest(ShiftController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShiftControllerPaginationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private ShiftService shiftService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private User testUser;
    private Restaurant testRestaurant;
    private ShiftResponseDTO testShiftDTO;

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

        testShiftDTO = ShiftResponseDTO.builder()
                .id(1L)
                .workerName("John Worker")
                .roleName("Line Cook")
                .status(ShiftStatus.SCHEDULED)
                .scheduledStart(LocalDateTime.now())
                .scheduledEnd(LocalDateTime.now().plusHours(8))
                .build();

        // ✅ Set up mock authentication BEFORE building MockMvc
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("manager@test.com", null, List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // ✅ Build MockMvc with security context
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        when(userService.findByEmail(any())).thenReturn(Optional.of(testUser));
    }

    @Test
    void testGetShiftsPaginated_NoFilters_ReturnsPage() throws Exception {
        Page<ShiftResponseDTO> page = new PageImpl<>(
                List.of(testShiftDTO),
                PageRequest.of(0, 10),
                1
        );

        when(shiftService.findShiftsPaginated(
                eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)
        )).thenReturn(page);

        mockMvc.perform(get("/api/shifts/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].workerName").value("John Worker"))
                .andExpect(jsonPath("$.content[0].roleName").value("Line Cook"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void testGetShiftsPaginated_WithWorkerNameFilter_ReturnsFilteredPage() throws Exception {
        Page<ShiftResponseDTO> page = new PageImpl<>(
                List.of(testShiftDTO),
                PageRequest.of(0, 10),
                1
        );

        when(shiftService.findShiftsPaginated(
                eq(1L), eq("John"), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)
        )).thenReturn(page);

        mockMvc.perform(get("/api/shifts/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .param("workerName", "John")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].workerName").value("John Worker"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetShiftsPaginated_WithStatusFilter_ReturnsFilteredPage() throws Exception {
        Page<ShiftResponseDTO> page = new PageImpl<>(
                List.of(testShiftDTO),
                PageRequest.of(0, 10),
                1
        );

        when(shiftService.findShiftsPaginated(
                eq(1L), isNull(), isNull(), eq(ShiftStatus.SCHEDULED), isNull(), isNull(), eq(0), eq(10)
        )).thenReturn(page);

        mockMvc.perform(get("/api/shifts/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "SCHEDULED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetShiftsPaginated_EmptyResult_ReturnsEmptyPage() throws Exception {
        Page<ShiftResponseDTO> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        when(shiftService.findShiftsPaginated(
                eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)
        )).thenReturn(emptyPage);

        mockMvc.perform(get("/api/shifts/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void testGetShiftsPaginated_WithRoleFilter_ReturnsFilteredPage() throws Exception {
        Page<ShiftResponseDTO> page = new PageImpl<>(
                List.of(testShiftDTO),
                PageRequest.of(0, 10),
                1
        );

        when(shiftService.findShiftsPaginated(
                eq(1L), isNull(), eq(1L), isNull(), isNull(), isNull(), eq(0), eq(10)
        )).thenReturn(page);

        mockMvc.perform(get("/api/shifts/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .param("roleId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].roleName").value("Line Cook"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}