package com.example.restaurantshifthandler.service;

import com.example.restaurantshifthandler.entity.BreakRequest;
import com.example.restaurantshifthandler.entity.Shift;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.entity.Restaurant;
import com.example.restaurantshifthandler.entity.Role;
import com.example.restaurantshifthandler.entity.CoverageRule;
import com.example.restaurantshifthandler.entity.enums.BreakStatus;
import com.example.restaurantshifthandler.entity.enums.BreakType;
import com.example.restaurantshifthandler.entity.enums.ShiftStatus;
import com.example.restaurantshifthandler.repository.BreakRequestRepository;
import com.example.restaurantshifthandler.repository.ShiftRepository;
import com.example.restaurantshifthandler.repository.CoverageRuleRepository;
import com.example.restaurantshifthandler.repository.UserRepository;
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
class BreakRequestServiceTest {

    @Mock
    private BreakRequestRepository repository;

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private CoverageRuleRepository coverageRuleRepository;

    @Mock
    private AlertService alertService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BreakRequestService breakRequestService;

    private BreakRequest testBreakRequest;
    private User testWorker;
    private Shift testShift;
    private Restaurant testRestaurant;
    private Role testRole;
    private CoverageRule testCoverageRule;

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
                .scheduledStart(LocalDateTime.now().minusHours(1))
                .scheduledEnd(LocalDateTime.now().plusHours(7))
                .status(ShiftStatus.ACTIVE)
                .clockInTime(LocalDateTime.now().minusHours(1))
                .build();

        testCoverageRule = CoverageRule.builder()
                .id(1L)
                .restaurant(testRestaurant)
                .role(testRole)
                .minimumWorkers(2)
                .build();

        testBreakRequest = BreakRequest.builder()
                .id(1L)
                .worker(testWorker)
                .shift(testShift)
                .breakType(BreakType.LUNCH)
                .status(BreakStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testSave_WithSufficientCoverage_AutoApproved() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testWorker));
        when(repository.findByWorkerId(1L)).thenReturn(Arrays.asList());
        when(shiftRepository.countByRoleIdAndStatus(1L, ShiftStatus.ACTIVE)).thenReturn(3);
        when(coverageRuleRepository.findByRestaurantIdAndRoleId(1L, 1L))
                .thenReturn(Optional.of(testCoverageRule));
        when(repository.save(any(BreakRequest.class))).thenReturn(testBreakRequest);

        // Act
        BreakRequest savedBreak = breakRequestService.save(testBreakRequest);

        // Assert
        assertNotNull(savedBreak);
        verify(repository, times(1)).save(any(BreakRequest.class));
    }

    @Test
    void testSave_WithInsufficientCoverage_Denied() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testWorker));
        when(repository.findByWorkerId(1L)).thenReturn(Arrays.asList());
        when(shiftRepository.countByRoleIdAndStatus(1L, ShiftStatus.ACTIVE)).thenReturn(2);
        when(coverageRuleRepository.findByRestaurantIdAndRoleId(1L, 1L))
                .thenReturn(Optional.of(testCoverageRule));
        when(repository.save(any(BreakRequest.class))).thenReturn(testBreakRequest);

        // Act
        BreakRequest savedBreak = breakRequestService.save(testBreakRequest);

        // Assert
        assertNotNull(savedBreak);
        verify(alertService, times(1)).save(any());
        verify(repository, times(1)).save(any(BreakRequest.class));
    }

    @Test
    void testSave_WithNoCoverageRule_AutoApproved() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testWorker));
        when(repository.findByWorkerId(1L)).thenReturn(Arrays.asList());
        when(shiftRepository.countByRoleIdAndStatus(1L, ShiftStatus.ACTIVE)).thenReturn(1);
        when(coverageRuleRepository.findByRestaurantIdAndRoleId(1L, 1L))
                .thenReturn(Optional.empty());
        when(repository.save(any(BreakRequest.class))).thenReturn(testBreakRequest);

        // Act
        BreakRequest savedBreak = breakRequestService.save(testBreakRequest);

        // Assert
        assertNotNull(savedBreak);
        verify(repository, times(1)).save(any(BreakRequest.class));
    }

    @Test
    void testSave_WithPendingBreak_ThrowsException() {
        // Arrange
        BreakRequest pendingBreak = BreakRequest.builder()
                .status(BreakStatus.PENDING)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testWorker));
        when(repository.findByWorkerId(1L)).thenReturn(Arrays.asList(pendingBreak));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            breakRequestService.save(testBreakRequest);
        });
    }

    @Test
    void testSave_WithActiveBreak_ThrowsException() {
        // Arrange
        BreakRequest activeBreak = BreakRequest.builder()
                .status(BreakStatus.ACTIVE)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testWorker));
        when(repository.findByWorkerId(1L)).thenReturn(Arrays.asList(activeBreak));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            breakRequestService.save(testBreakRequest);
        });
    }

    @Test
    void testFindById_BreakExists_ReturnsBreak() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testBreakRequest));

        // Act
        Optional<BreakRequest> result = breakRequestService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(BreakType.LUNCH, result.get().getBreakType());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void testStart_Success() {
        // Arrange
        testBreakRequest.setStatus(BreakStatus.APPROVED);
        when(repository.findById(1L)).thenReturn(Optional.of(testBreakRequest));
        when(repository.save(any(BreakRequest.class))).thenReturn(testBreakRequest);

        // Act
        BreakRequest startedBreak = breakRequestService.start(1L);

        // Assert
        assertNotNull(startedBreak);
        assertEquals(BreakStatus.ACTIVE, startedBreak.getStatus());
        assertNotNull(startedBreak.getStartTime());
        verify(repository, times(1)).save(any(BreakRequest.class));
    }

    @Test
    void testComplete_Success() {
        // Arrange
        testBreakRequest.setStatus(BreakStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(testBreakRequest));
        when(repository.save(any(BreakRequest.class))).thenReturn(testBreakRequest);

        // Act
        BreakRequest completedBreak = breakRequestService.complete(1L);

        // Assert
        assertNotNull(completedBreak);
        assertEquals(BreakStatus.COMPLETED, completedBreak.getStatus());
        assertNotNull(completedBreak.getEndTime());
        verify(repository, times(1)).save(any(BreakRequest.class));
    }

    @Test
    void testDeleteById_PendingStatus_Success() {
        // Arrange
        testBreakRequest.setStatus(BreakStatus.PENDING);
        when(repository.findById(1L)).thenReturn(Optional.of(testBreakRequest));
        doNothing().when(repository).deleteById(1L);

        // Act
        breakRequestService.deleteById(1L);

        // Assert
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteById_ActiveStatus_ThrowsException() {
        // Arrange
        testBreakRequest.setStatus(BreakStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(testBreakRequest));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            breakRequestService.deleteById(1L);
        });
    }

    @Test
    void testDeleteById_CompletedStatus_ThrowsException() {
        // Arrange
        testBreakRequest.setStatus(BreakStatus.COMPLETED);
        when(repository.findById(1L)).thenReturn(Optional.of(testBreakRequest));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            breakRequestService.deleteById(1L);
        });
    }

    @Test
    void testFindByWorker_ReturnsBreakList() {
        // Arrange
        List<BreakRequest> breaks = Arrays.asList(testBreakRequest);
        when(repository.findByWorkerId(1L)).thenReturn(breaks);

        // Act
        List<BreakRequest> result = breakRequestService.findByWorker(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findByWorkerId(1L);
    }

    @Test
    void testUpdateExpiredBreaks_AutoCompleteActiveBreak() {
        // Arrange
        BreakRequest expiredBreak = BreakRequest.builder()
                .id(2L)
                .status(BreakStatus.ACTIVE)
                .endTime(LocalDateTime.now().minusMinutes(10))
                .build();

        when(repository.findByStatus(BreakStatus.ACTIVE)).thenReturn(Arrays.asList(expiredBreak));
        when(repository.findByStatus(BreakStatus.PENDING)).thenReturn(Arrays.asList());
        when(repository.save(any(BreakRequest.class))).thenReturn(expiredBreak);

        // Act
        breakRequestService.updateExpiredBreaks();

        // Assert
        verify(repository, times(1)).save(argThat(breakReq ->
                breakReq.getStatus() == BreakStatus.COMPLETED
        ));
    }

    @Test
    void testApprove_Success() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testBreakRequest));
        when(repository.save(any(BreakRequest.class))).thenReturn(testBreakRequest);

        // Act
        BreakRequest approvedBreak = breakRequestService.approve(1L);

        // Assert
        assertNotNull(approvedBreak);
        assertEquals(BreakStatus.APPROVED, approvedBreak.getStatus());
        verify(repository, times(1)).save(any(BreakRequest.class));
    }

    @Test
    void testDeny_Success() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testBreakRequest));
        when(repository.save(any(BreakRequest.class))).thenReturn(testBreakRequest);

        // Act
        BreakRequest deniedBreak = breakRequestService.deny(1L);

        // Assert
        assertNotNull(deniedBreak);
        assertEquals(BreakStatus.DENIED, deniedBreak.getStatus());
        verify(repository, times(1)).save(any(BreakRequest.class));
    }
}