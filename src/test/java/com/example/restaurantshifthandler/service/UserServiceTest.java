package com.example.restaurantshifthandler.service;

import com.example.restaurantshifthandler.dto.UserDTO;
import com.example.restaurantshifthandler.entity.Restaurant;
import com.example.restaurantshifthandler.entity.Role;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.entity.enums.SalaryType;
import com.example.restaurantshifthandler.exception.ResourceNotFoundException;
import com.example.restaurantshifthandler.repository.RestaurantRepository;
import com.example.restaurantshifthandler.repository.RoleRepository;
import com.example.restaurantshifthandler.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;
    private Restaurant testRestaurant;
    private UserDTO testUserDTO;

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
                .password("$2a$10$hashedPassword")
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
    void testFindAll_ReturnsAllUsers() {
        when(repository.findAll()).thenReturn(List.of(testUser));

        List<User> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        verify(repository, times(1)).findAll();
    }

    @Test
    void testFindById_ExistingId_ReturnsUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NonExistingId_ReturnsEmpty() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(99L);

        assertThat(result).isEmpty();
        verify(repository, times(1)).findById(99L);
    }

    @Test
    void testFindByEmail_ExistingEmail_ReturnsUser() {
        when(repository.findByEmail("john@test.com")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByEmail("john@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@test.com");
        verify(repository, times(1)).findByEmail("john@test.com");
    }

    @Test
    void testFindByEmail_NonExistingEmail_ReturnsEmpty() {
        when(repository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("nonexistent@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    void testExistsByEmail_ExistingEmail_ReturnsTrue() {
        when(repository.existsByEmail("john@test.com")).thenReturn(true);

        boolean result = userService.existsByEmail("john@test.com");

        assertThat(result).isTrue();
        verify(repository, times(1)).existsByEmail("john@test.com");
    }

    @Test
    void testExistsByEmail_NonExistingEmail_ReturnsFalse() {
        when(repository.existsByEmail("nonexistent@test.com")).thenReturn(false);

        boolean result = userService.existsByEmail("nonexistent@test.com");

        assertThat(result).isFalse();
    }

    @Test
    void testSave_ValidDTO_ReturnsUser() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(repository.save(any(User.class))).thenReturn(testUser);

        User result = userService.save(testUserDTO, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Doe");
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void testSave_WithSalaryFields_ReturnsSavedUser() {
        testUserDTO.setSalaryType(SalaryType.HOURLY);
        testUserDTO.setHourlyRate(15.0);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(repository.save(any(User.class))).thenReturn(testUser);

        User result = userService.save(testUserDTO, 1L);

        assertThat(result).isNotNull();
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void testSave_RoleNotFound_ThrowsException() {
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.save(testUserDTO, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role not found");

        verify(repository, never()).save(any(User.class));
    }

    @Test
    void testUpdate_ValidDTO_ReturnsUpdatedUser() {
        UserDTO updateDTO = UserDTO.builder()
                .name("Jane Doe")
                .email("jane@test.com")
                .roleId(1L)
                .isActive(true)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(repository.save(any(User.class))).thenReturn(testUser);

        User result = userService.update(1L, updateDTO, 2L);

        assertThat(result).isNotNull();
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdate_SameUserAsCurrentUser_ThrowsException() {
        assertThatThrownBy(() -> userService.update(1L, testUserDTO, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You cannot edit your own account");

        verify(repository, never()).save(any(User.class));
    }

    @Test
    void testUpdate_UserNotFound_ThrowsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, testUserDTO, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(repository, never()).save(any(User.class));
    }

    @Test
    void testUpdate_WithSalaryFields_UpdatesSalary() {
        testUserDTO.setSalaryType(SalaryType.MONTHLY);
        testUserDTO.setMonthlySalary(5000.0);

        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(repository.save(any(User.class))).thenReturn(testUser);

        User result = userService.update(1L, testUserDTO, 2L);

        assertThat(result).isNotNull();
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void testDeleteById_ValidId_DeletesUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteById(1L, 2L);

        verify(repository, times(1)).delete(testUser);
    }

    @Test
    void testDeleteById_SameUserAsCurrentUser_ThrowsException() {
        assertThatThrownBy(() -> userService.deleteById(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cannot delete your own account");

        verify(repository, never()).delete(any(User.class));
    }

    @Test
    void testDeleteById_UserNotFound_ThrowsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(repository, never()).delete(any(User.class));
    }

    @Test
    void testToggleActive_ActiveUser_DeactivatesUser() {
        testUser.setIsActive(true);
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        when(repository.save(any(User.class))).thenReturn(testUser);

        User result = userService.toggleActive(1L, 2L);

        assertThat(result).isNotNull();
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void testToggleActive_SameUserAsCurrentUser_ThrowsException() {
        assertThatThrownBy(() -> userService.toggleActive(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cannot deactivate your own account");

        verify(repository, never()).save(any(User.class));
    }

    @Test
    void testToggleActive_UserNotFound_ThrowsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.toggleActive(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void testFindByRestaurantId_ReturnsUsers() {
        when(repository.findByRestaurantId(1L)).thenReturn(List.of(testUser));

        List<User> result = userService.findByRestaurantId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        verify(repository, times(1)).findByRestaurantId(1L);
    }
}