package com.example.restaurantshifthandler.service;

import com.example.restaurantshifthandler.dto.ShiftDTO;
import com.example.restaurantshifthandler.entity.Shift;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.entity.Restaurant;
import com.example.restaurantshifthandler.entity.Role;
import com.example.restaurantshifthandler.entity.enums.ShiftStatus;
import com.example.restaurantshifthandler.repository.ShiftRepository;
import com.example.restaurantshifthandler.repository.UserRepository;
import com.example.restaurantshifthandler.repository.RoleRepository;
import com.example.restaurantshifthandler.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    private ShiftRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private ShiftService shiftService;

    private Shift testShift;
    private ShiftDTO testShiftDTO;
    private User testWorker;
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

        testWorker = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@test.com")
                .role(testRole)
                .restaurant(testRestaurant)
                .isActive(true)
                .build();

        testShift = Shift.builder()
                .id(1L)
                .worker(testWorker)
                .role(testRole)
                .restaurant(testRestaurant)
                .scheduledStart(LocalDateTime.now().plusHours(1))
                .scheduledEnd(LocalDateTime.now().plusHours(9))
                .status(ShiftStatus.SCHEDULED)
                .build();

        testShiftDTO = ShiftDTO.builder()
                .workerId(1L)
                .roleId(1L)
                .scheduledStart(LocalDateTime.now().plusHours(1))
                .scheduledEnd(LocalDateTime.now().plusHours(9))
                .status(ShiftStatus.SCHEDULED)
                .build();
    }

    @Test
    void testSave_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testWorker));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(repository.save(any(Shift.class))).thenReturn(testShift);

        // Act
        Shift savedShift = shiftService.save(testShiftDTO, 1L);

        // Assert
        assertNotNull(savedShift);
        assertEquals(ShiftStatus.SCHEDULED, savedShift.getStatus());
        verify(repository, times(1)).save(any(Shift.class));
    }

    @Test
    void testFindById_ShiftExists_ReturnsShift() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testShift));

        // Act
        Optional<Shift> result = shiftService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ShiftStatus.SCHEDULED, result.get().getStatus());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void testFindById_ShiftNotFound_ReturnsEmpty() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Shift> result = shiftService.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    void testClockIn_Success() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testShift));
        when(repository.save(any(Shift.class))).thenReturn(testShift);

        // Act
        Shift clockedInShift = shiftService.clockIn(1L);

        // Assert
        assertNotNull(clockedInShift);
        assertNotNull(clockedInShift.getClockInTime());
        assertEquals(ShiftStatus.ACTIVE, clockedInShift.getStatus());
        verify(repository, times(1)).save(any(Shift.class));
    }

    @Test
    void testClockOut_Success() {
        // Arrange
        testShift.setStatus(ShiftStatus.ACTIVE);
        testShift.setClockInTime(LocalDateTime.now().minusHours(1));
        when(repository.findById(1L)).thenReturn(Optional.of(testShift));
        when(repository.save(any(Shift.class))).thenReturn(testShift);

        // Act
        Shift clockedOutShift = shiftService.clockOut(1L);

        // Assert
        assertNotNull(clockedOutShift);
        assertNotNull(clockedOutShift.getClockOutTime());
        assertEquals(ShiftStatus.COMPLETED, clockedOutShift.getStatus());
        verify(repository, times(1)).save(any(Shift.class));
    }

    @Test
    void testCancel_Success() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testShift));
        when(repository.save(any(Shift.class))).thenReturn(testShift);

        // Act
        Shift cancelledShift = shiftService.cancel(1L);

        // Assert
        assertNotNull(cancelledShift);
        assertEquals(ShiftStatus.CANCELLED, cancelledShift.getStatus());
        verify(repository, times(1)).save(any(Shift.class));
    }

    @Test
    void testFindByWorker_ReturnsShiftList() {
        // Arrange
        List<Shift> shifts = Arrays.asList(testShift);
        when(repository.findByWorkerId(1L)).thenReturn(shifts);

        // Act
        List<Shift> result = shiftService.findByWorker(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findByWorkerId(1L);
    }

    @Test
    void testFindByRestaurantId_ReturnsShiftList() {
        // Arrange
        List<Shift> shifts = Arrays.asList(testShift);
        when(repository.findByRestaurantId(1L)).thenReturn(shifts);

        // Act
        List<Shift> result = shiftService.findByRestaurantId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findByRestaurantId(1L);
    }

    @Test
    void testFindByStatus_ReturnsShiftList() {
        // Arrange
        List<Shift> shifts = Arrays.asList(testShift);
        when(repository.findByStatus(ShiftStatus.SCHEDULED)).thenReturn(shifts);

        // Act
        List<Shift> result = shiftService.findByStatus(ShiftStatus.SCHEDULED);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findByStatus(ShiftStatus.SCHEDULED);
    }

    @Test
    void testDeleteById_Success() {
        // Arrange
        doNothing().when(repository).deleteById(1L);

        // Act
        shiftService.deleteById(1L);

        // Assert
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testShift));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testWorker));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(repository.save(any(Shift.class))).thenReturn(testShift);

        // Act
        Shift updatedShift = shiftService.update(1L, testShiftDTO, 1L);

        // Assert
        assertNotNull(updatedShift);
        verify(repository, times(1)).save(any(Shift.class));
    }

    @Test
    void testUpdateExpiredShifts_MissedScheduledShift() {
        // Arrange
        Shift expiredShift = Shift.builder()
                .id(2L)
                .worker(testWorker)
                .role(testRole)
                .restaurant(testRestaurant)
                .scheduledStart(LocalDateTime.now().minusHours(10))
                .scheduledEnd(LocalDateTime.now().minusHours(2))
                .status(ShiftStatus.SCHEDULED)
                .build();

        when(repository.findByStatus(ShiftStatus.SCHEDULED)).thenReturn(Arrays.asList(expiredShift));
        when(repository.findByStatus(ShiftStatus.ACTIVE)).thenReturn(Arrays.asList());
        when(repository.save(any(Shift.class))).thenReturn(expiredShift);

        // Act
        shiftService.updateExpiredShifts();

        // Assert
        verify(repository, times(1)).save(argThat(shift ->
                shift.getStatus() == ShiftStatus.MISSED
        ));
    }

    @Test
    void testUpdateExpiredShifts_AutoCompleteActiveShift() {
        // Arrange
        Shift activeShift = Shift.builder()
                .id(3L)
                .worker(testWorker)
                .role(testRole)
                .restaurant(testRestaurant)
                .scheduledStart(LocalDateTime.now().minusHours(10))
                .scheduledEnd(LocalDateTime.now().minusHours(1))
                .status(ShiftStatus.ACTIVE)
                .clockInTime(LocalDateTime.now().minusHours(10))
                .build();

        when(repository.findByStatus(ShiftStatus.SCHEDULED)).thenReturn(Arrays.asList());
        when(repository.findByStatus(ShiftStatus.ACTIVE)).thenReturn(Arrays.asList(activeShift));
        when(repository.save(any(Shift.class))).thenReturn(activeShift);

        // Act
        shiftService.updateExpiredShifts();

        // Assert
        verify(repository, times(1)).save(argThat(shift ->
                shift.getStatus() == ShiftStatus.COMPLETED &&
                        shift.getClockOutTime() != null
        ));
    }
}
