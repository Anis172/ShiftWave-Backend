package com.example.restaurantshifthandler.controller;



import com.example.restaurantshifthandler.dto.LoginRequest;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.entity.Role;
import com.example.restaurantshifthandler.entity.Restaurant;
import com.example.restaurantshifthandler.security.JwtUtil;
import com.example.restaurantshifthandler.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security for testing
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;


    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Role testRole;
    private Restaurant testRestaurant;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        testRestaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .build();

        testRole = Role.builder()
                .id(1L)
                .name("Manager")
                .build();

        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@test.com")
                .password("$2a$10$hashedPassword") // BCrypt hashed password
                .role(testRole)
                .restaurant(testRestaurant)
                .isActive(true)
                .build();

        validLoginRequest = LoginRequest.builder()
                .email("john@test.com")
                .password("password123")
                .build();
    }

    @Test
    void testLogin_ValidCredentials_ReturnsTokenAndUserInfo() throws Exception {
        // Arrange
        when(userService.findByEmail("john@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("john@test.com")).thenReturn("mock-jwt-token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.email").value("john@test.com"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.role").value("Manager"));

        verify(userService, times(1)).findByEmail("john@test.com");
        verify(passwordEncoder, times(1)).matches("password123", testUser.getPassword());
        verify(jwtUtil, times(1)).generateToken("john@test.com");
    }

    @Test
    void testLogin_InvalidEmail_ReturnsError() throws Exception {
        // Arrange
        when(userService.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        LoginRequest invalidRequest = LoginRequest.builder()
                .email("nonexistent@test.com")
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));

        verify(userService, times(1)).findByEmail("nonexistent@test.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLogin_InvalidPassword_ReturnsError() throws Exception {
        // Arrange
        when(userService.findByEmail("john@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        LoginRequest wrongPasswordRequest = LoginRequest.builder()
                .email("john@test.com")
                .password("wrongpassword")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));

        verify(userService, times(1)).findByEmail("john@test.com");
        verify(passwordEncoder, times(1)).matches("wrongpassword", testUser.getPassword());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testLogin_InactiveUser_ReturnsError() throws Exception {
        // Arrange
        testUser.setIsActive(false); // Deactivated user
        when(userService.findByEmail("john@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Account is inactive. Please contact your manager."));

        verify(userService, times(1)).findByEmail("john@test.com");
        verify(passwordEncoder, times(1)).matches("password123", testUser.getPassword());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testLogin_MissingEmail_ReturnsValidationError() throws Exception {
        // Arrange
        LoginRequest missingEmailRequest = LoginRequest.builder()
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(userService, never()).findByEmail(anyString());
    }

    @Test
    void testLogin_MissingPassword_ReturnsValidationError() throws Exception {
        // Arrange
        LoginRequest missingPasswordRequest = LoginRequest.builder()
                .email("john@test.com")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(userService, never()).findByEmail(anyString());
    }

    @Test
    void testLogin_InvalidEmailFormat_ReturnsValidationError() throws Exception {
        // Arrange
        LoginRequest invalidEmailRequest = LoginRequest.builder()
                .email("notanemail")
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(userService, never()).findByEmail(anyString());
    }
}