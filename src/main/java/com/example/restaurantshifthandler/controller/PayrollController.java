package com.example.restaurantshifthandler.controller;



import com.example.restaurantshifthandler.entity.PayrollRecord;
import com.example.restaurantshifthandler.entity.User;
import com.example.restaurantshifthandler.exception.ResourceNotFoundException;
import com.example.restaurantshifthandler.service.PayrollService;
import com.example.restaurantshifthandler.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final UserService userService;

    //  Get payroll for all workers for a specific month/year
    @GetMapping
    public ResponseEntity<List<PayrollRecord>> getPayroll(
            @RequestParam Integer month,
            @RequestParam Integer year) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();

        List<PayrollRecord> records = payrollService.calculateMonthlyPayroll(
                restaurantId, month, year);

        return ResponseEntity.ok(records);
    }

    //  Mark worker as paid for a specific month/year
    @PostMapping("/mark-paid")
    public ResponseEntity<PayrollRecord> markAsPaid(
            @RequestParam Long workerId,
            @RequestParam Integer month,
            @RequestParam Integer year) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long restaurantId = currentUser.getRestaurant().getId();

        PayrollRecord record = payrollService.markAsPaid(
                workerId, month, year, restaurantId);

        return ResponseEntity.ok(record);
    }

    //  Mark worker as unpaid for a specific month/year
    @PostMapping("/mark-unpaid")
    public ResponseEntity<PayrollRecord> markAsUnpaid(
            @RequestParam Long workerId,
            @RequestParam Integer month,
            @RequestParam Integer year) {

        PayrollRecord record = payrollService.markAsUnpaid(
                workerId, month, year);

        return ResponseEntity.ok(record);
    }
}
