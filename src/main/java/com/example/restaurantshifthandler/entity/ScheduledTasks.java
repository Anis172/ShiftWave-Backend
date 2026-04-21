package com.example.restaurantshifthandler.entity;


import com.example.restaurantshifthandler.service.BreakRequestService;
import com.example.restaurantshifthandler.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final ShiftService shiftService;
    private final BreakRequestService breakRequestService;

    @Scheduled(fixedRate = 300000)
    public void updateExpiredShiftsAndBreaks() {
        shiftService.updateExpiredShifts();
        breakRequestService.updateExpiredBreaks();
    }
}