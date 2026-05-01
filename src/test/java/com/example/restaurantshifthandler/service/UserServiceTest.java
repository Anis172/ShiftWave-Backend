package com.example.restaurantshifthandler.service;

import com.example.restaurantshifthandler.dto.UserDTO;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.entity.Restaurant;
import com.example.restaurantshifthandler.entity.Role;
import com.example.restaurantshifthandler.repository.UserRepository;
import com.example.restaurantshifthandler.repository.RoleRepository;
import com.example.restaurantshifthandler.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private Restaurant testRestaurant;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRestaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .build();

        testRole = Role.builder()
                .id(1L)
                .name("Line Cook")
                .build();

        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@test.com")
                .password("hashedPassword")
                .role(testRole)
                .restaurant(testRestaurant)
                .isActive(true)
                .build();

        testUserDTO = UserDTO.builder()
                .name("John Doe")
                .email("john@test.com")
                .password("password123")
                .roleId(1L)
                .isActive(true)
                .build();
    }

    @Test
    void testSave_Success() {
        // Arrange
        when(repository.existsByEmail("john@test.com")).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(repository.save(any(User.class))).thenReturn(testUser);

        // Act
        User savedUser = userService.save(testUserDTO, 1L);

        // Assert
        assertNotNull(savedUser);
        assertEquals("John Doe", savedUser.getName());
        assertEquals("john@test.com", savedUser.getEmail());
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void testSave_EmailAlreadyExists_ThrowsException() {
        // Arrange
        when(repository.existsByEmail("john@test.com")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.save(testUserDTO, 1L);
        });
    }

    @Test
    void testSave_PasswordTooShort_ThrowsException() {
        // Arrange
        testUserDTO.setPassword("short");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.save(testUserDTO, 1L);
        });
        assertEquals("Password must be at least 8 characters", exception.getMessage());
    }

    @Test
    void testSave_PasswordNull_ThrowsException() {
        // Arrange
        testUserDTO.setPassword(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.save(testUserDTO, 1L);
        });
        assertEquals("Password is required", exception.getMessage());
    }

    @Test
    void testSave_PasswordEmpty_ThrowsException() {
        // Arrange
        testUserDTO.setPassword("   ");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.save(testUserDTO, 1L);
        });
        assertEquals("Password is required", exception.getMessage());
    }

    @Test
    void testFindById_UserExists_ReturnsUser() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void testFindById_UserNotFound_ReturnsEmpty() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    void testFindByEmail_UserExists_ReturnsUser() {
        // Arrange
        when(repository.findByEmail("john@test.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByEmail("john@test.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("john@test.com", result.get().getEmail());
        verify(repository, times(1)).findByEmail("john@test.com");
    }

    @Test
    void testFindByRestaurantId_ReturnsUserList() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(repository.findByRestaurantId(1L)).thenReturn(users);

        // Act
        List<User> result = userService.findByRestaurantId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(repository, times(1)).findByRestaurantId(1L);
    }

    @Test
    void testDeleteById_Success() {
        // Arrange
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        // Act
        userService.deleteById(1L, 2L); // Different user IDs

        // Assert
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteById_SelfDeletion_ThrowsException() {
        // Arrange - Same user ID
        Long userId = 1L;

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteById(userId, userId);
        });
        assertEquals("You cannot delete your own account", exception.getMessage());
    }

    @Test
    void testDeleteById_UserNotFound_ThrowsException() {
        // Arrange
        when(repository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteById(999L, 1L);
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testExistsByEmail_EmailExists_ReturnsTrue() {
        // Arrange
        when(repository.existsByEmail("john@test.com")).thenReturn(true);

        // Act
        boolean exists = userService.existsByEmail("john@test.com");

        // Assert
        assertTrue(exists);
        verify(repository, times(1)).existsByEmail("john@test.com");
    }

    @Test
    void testExistsByEmail_EmailNotExists_ReturnsFalse() {
        // Arrange
        when(repository.existsByEmail("notfound@test.com")).thenReturn(false);

        // Act
        boolean exists = userService.existsByEmail("notfound@test.com");

        // Assert
        assertFalse(exists);
        verify(repository, times(1)).existsByEmail("notfound@test.com");
    }

    @Test

    void testUpdate_Success() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(repository.save(any(User.class))).thenReturn(testUser);

        // Act
        User updatedUser = userService.update(1L, testUserDTO, 1L);

        // Assert
        assertNotNull(updatedUser);
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdate_EmailChanged_DuplicateEmail_ThrowsException() {
        // Arrange
        testUser.setEmail("old@test.com");
        testUserDTO.setEmail("new@test.com");

        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        when(repository.existsByEmail("new@test.com")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.update(1L, testUserDTO, 1L);
        });
    }

    @Test
    void testUpdate_PasswordTooShort_ThrowsException() {
        // Arrange
        testUserDTO.setPassword("short");
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.update(1L, testUserDTO, 1L);
        });
        assertEquals("Password must be at least 8 characters", exception.getMessage());
    }

    @Test
    void testToggleActive_Success() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        when(repository.save(any(User.class))).thenReturn(testUser);

        // Act
        User toggledUser = userService.toggleActive(1L, 2L); // Different user IDs

        // Assert
        assertNotNull(toggledUser);
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void testToggleActive_SelfDeactivation_ThrowsException() {
        // Arrange - Same user ID
        Long userId = 1L;

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.toggleActive(userId, userId);
        });
        assertEquals("You cannot deactivate your own account", exception.getMessage());
    }
}