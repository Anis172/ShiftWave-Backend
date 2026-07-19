package com.example.restaurantshifthandler.service;

import com.example.restaurantshifthandler.entity.PayrollRecord;
import com.example.restaurantshifthandler.entity.Shift;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.entity.enums.BreakType;
import com.example.restaurantshifthandler.entity.enums.SalaryType;
import com.example.restaurantshifthandler.entity.enums.ShiftStatus;
import com.example.restaurantshifthandler.repository.BreakRequestRepository;
import com.example.restaurantshifthandler.repository.PayrollRepository;
import com.example.restaurantshifthandler.repository.ShiftRepository;
import com.example.restaurantshifthandler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final ShiftRepository shiftRepository;
    private final BreakRequestRepository breakRequestRepository;
    private final UserRepository userRepository;

    // Calculate payroll for ALL workers in a restaurant for a specific month/year
    public List<PayrollRecord> calculateMonthlyPayroll(
            Long restaurantId,
            Integer month,
            Integer year) {

        // Get all active workers in the restaurant
        List<User> workers = userRepository.findByRestaurantIdAndIsActive(restaurantId, true);

        List<PayrollRecord> records = new ArrayList<>();

        for (User worker : workers) {

            // Skip workers with no salary configured
            if (worker.getSalaryType() == null) continue;

            // Check if payroll record already exists for this month
            Optional<PayrollRecord> existing = payrollRepository
                    .findByWorkerIdAndMonthAndYear(worker.getId(), month, year);

            if (existing.isPresent()) {
                records.add(existing.get());
                continue;
            }

            // Get all COMPLETED shifts for this worker in this month/year
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            List<Shift> completedShifts = shiftRepository
                    .findByWorkerIdAndStatusAndClockOutTimeBetween(
                            worker.getId(),
                            ShiftStatus.COMPLETED,
                            startOfMonth,
                            endOfMonth
                    );

            // Calculate based on salary type
            double totalHours = 0;
            double totalDays = completedShifts.size();
            double totalSalary = 0;

            if (worker.getSalaryType() == SalaryType.HOURLY) {
                // Calculate total paid hours
                for (Shift shift : completedShifts) {
                    if (shift.getClockInTime() == null || shift.getClockOutTime() == null) continue;

                    double shiftHours = Duration.between(
                            shift.getClockInTime(),
                            shift.getClockOutTime()
                    ).toMinutes() / 60.0;

                    // Subtract unpaid breaks (LUNCH and PERSONAL)
                    double unpaidBreakHours = breakRequestRepository
                            .findByShiftId(shift.getId())
                            .stream()
                            .filter(br -> br.getStartTime() != null && br.getEndTime() != null)
                            .filter(br -> !isBreakPaid(br.getBreakType()))
                            .mapToDouble(br -> Duration.between(
                                    br.getStartTime(),
                                    br.getEndTime()
                            ).toMinutes() / 60.0)
                            .sum();

                    totalHours += shiftHours - unpaidBreakHours;
                }

                totalSalary = totalHours * (worker.getHourlyRate() != null ? worker.getHourlyRate() : 0);

            } else if (worker.getSalaryType() == SalaryType.DAILY) {
                totalSalary = totalDays * (worker.getDailyRate() != null ? worker.getDailyRate() : 0);

                // Still calculate hours for transparency
                for (Shift shift : completedShifts) {
                    if (shift.getClockInTime() == null || shift.getClockOutTime() == null) continue;
                    totalHours += Duration.between(
                            shift.getClockInTime(),
                            shift.getClockOutTime()
                    ).toMinutes() / 60.0;
                }

            } else if (worker.getSalaryType() == SalaryType.MONTHLY) {
                totalSalary = worker.getMonthlySalary() != null ? worker.getMonthlySalary() : 0;

                // Still calculate hours for transparency
                for (Shift shift : completedShifts) {
                    if (shift.getClockInTime() == null || shift.getClockOutTime() == null) continue;
                    totalHours += Duration.between(
                            shift.getClockInTime(),
                            shift.getClockOutTime()
                    ).toMinutes() / 60.0;
                }
            }

            // Build payroll record (not saved yet - calculated dynamically)
            PayrollRecord record = PayrollRecord.builder()
                    .worker(worker)
                    .restaurant(worker.getRestaurant())
                    .month(month)
                    .year(year)
                    .totalHours(Math.round(totalHours * 100.0) / 100.0)
                    .totalDays(totalDays)
                    .totalSalary(Math.round(totalSalary * 100.0) / 100.0)
                    .isPaid(false)
                    .build();

            records.add(record);
        }

        return records;
    }

    //  Mark payroll as paid (saves to database)
    public PayrollRecord markAsPaid(
            Long workerId,
            Integer month,
            Integer year,
            Long restaurantId) {

        // Check if record exists
        Optional<PayrollRecord> existing = payrollRepository
                .findByWorkerIdAndMonthAndYear(workerId, month, year);

        if (existing.isPresent()) {
            PayrollRecord record = existing.get();
            record.setIsPaid(true);
            record.setPaidAt(LocalDateTime.now());
            return payrollRepository.save(record);
        }

        // Calculate and save
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Shift> completedShifts = shiftRepository
                .findByWorkerIdAndStatusAndClockOutTimeBetween(
                        worker.getId(),
                        ShiftStatus.COMPLETED,
                        startOfMonth,
                        endOfMonth
                );

        double totalHours = 0;
        double totalDays = completedShifts.size();
        double totalSalary = 0;

        if (worker.getSalaryType() == SalaryType.HOURLY) {
            for (Shift shift : completedShifts) {
                if (shift.getClockInTime() == null || shift.getClockOutTime() == null) continue;

                double shiftHours = Duration.between(
                        shift.getClockInTime(),
                        shift.getClockOutTime()
                ).toMinutes() / 60.0;

                double unpaidBreakHours = breakRequestRepository
                        .findByShiftId(shift.getId())
                        .stream()
                        .filter(br -> br.getStartTime() != null && br.getEndTime() != null)
                        .filter(br -> !isBreakPaid(br.getBreakType()))
                        .mapToDouble(br -> Duration.between(
                                br.getStartTime(),
                                br.getEndTime()
                        ).toMinutes() / 60.0)
                        .sum();

                totalHours += shiftHours - unpaidBreakHours;
            }
            totalSalary = totalHours * (worker.getHourlyRate() != null ? worker.getHourlyRate() : 0);

        } else if (worker.getSalaryType() == SalaryType.DAILY) {
            totalSalary = totalDays * (worker.getDailyRate() != null ? worker.getDailyRate() : 0);
            for (Shift shift : completedShifts) {
                if (shift.getClockInTime() == null || shift.getClockOutTime() == null) continue;
                totalHours += Duration.between(
                        shift.getClockInTime(),
                        shift.getClockOutTime()
                ).toMinutes() / 60.0;
            }

        } else if (worker.getSalaryType() == SalaryType.MONTHLY) {
            totalSalary = worker.getMonthlySalary() != null ? worker.getMonthlySalary() : 0;
            for (Shift shift : completedShifts) {
                if (shift.getClockInTime() == null || shift.getClockOutTime() == null) continue;
                totalHours += Duration.between(
                        shift.getClockInTime(),
                        shift.getClockOutTime()
                ).toMinutes() / 60.0;
            }
        }

        PayrollRecord record = PayrollRecord.builder()
                .worker(worker)
                .restaurant(worker.getRestaurant())
                .month(month)
                .year(year)
                .totalHours(Math.round(totalHours * 100.0) / 100.0)
                .totalDays(totalDays)
                .totalSalary(Math.round(totalSalary * 100.0) / 100.0)
                .isPaid(true)
                .paidAt(LocalDateTime.now())
                .build();

        return payrollRepository.save(record);
    }

    //  Mark as unpaid
    public PayrollRecord markAsUnpaid(Long workerId, Integer month, Integer year) {
        PayrollRecord record = payrollRepository
                .findByWorkerIdAndMonthAndYear(workerId, month, year)
                .orElseThrow(() -> new RuntimeException("Payroll record not found"));

        record.setIsPaid(false);
        record.setPaidAt(null);
        return payrollRepository.save(record);
    }

    //  Which breaks are NOT paid
    private boolean isBreakPaid(BreakType breakType) {
        return switch (breakType) {
            case SHORT -> true;
            case EMERGENCY -> true;
            case SICK_LEAVE -> true;
            case LUNCH -> false;
            case PERSONAL -> false;
        };
    }
}