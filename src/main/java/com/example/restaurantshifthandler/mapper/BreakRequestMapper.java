package com.example.restaurantshifthandler.mapper;

import com.example.restaurantshifthandler.dto.BreakRequestDTO;
import com.example.restaurantshifthandler.entity.BreakRequest;
import com.example.restaurantshifthandler.entity.Shift;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.repository.ShiftRepository;
import com.example.restaurantshifthandler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BreakRequestMapper {

    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;

    public BreakRequestDTO toDTO(BreakRequest breakRequest) {
        if (breakRequest == null) {
            return null;
        }

        return BreakRequestDTO.builder()
                .id(breakRequest.getId())
                .workerId(breakRequest.getWorker().getId())
                .workerName(breakRequest.getWorker().getName())
                .shiftId(breakRequest.getShift().getId())
                .breakType(breakRequest.getBreakType())
                .status(breakRequest.getStatus())
                .requestedAt(breakRequest.getRequestedAt())
                .startTime(breakRequest.getStartTime())
                .endTime(breakRequest.getEndTime())
                .build();
    }

    // ✅ NEW: Convert DTO to Entity
    public BreakRequest toEntity(BreakRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        // Fetch worker and shift from database
        User worker = userRepository.findById(dto.getWorkerId())
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        Shift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        return BreakRequest.builder()
                .id(dto.getId())
                .worker(worker)
                .shift(shift)
                .breakType(dto.getBreakType())
                .status(dto.getStatus())
                .requestedAt(dto.getRequestedAt())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }
}