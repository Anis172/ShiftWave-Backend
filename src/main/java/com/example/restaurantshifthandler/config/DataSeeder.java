package com.example.restaurantshifthandler.config;

import com.example.restaurantshifthandler.entity.Role;
import com.example.restaurantshifthandler.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        seedRoles();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            List<RoleData> roles = Arrays.asList(
                    new RoleData("Head Chef", "Kitchen"),
                    new RoleData("Sous Chef", "Kitchen"),
                    new RoleData("Line Cook", "Kitchen"),
                    new RoleData("Prep Cook", "Kitchen"),
                    new RoleData("Dishwasher", "Kitchen"),
                    new RoleData("Waiter/Server", "Floor"),
                    new RoleData("Host/Hostess", "Floor"),
                    new RoleData("Busboy", "Floor"),
                    new RoleData("Bartender", "Bar"),
                    new RoleData("Barback", "Bar"),
                    new RoleData("Manager", "Management"),
                    new RoleData("Shift Supervisor", "Management"),
                    new RoleData("Cashier", "Other"),
                    new RoleData("Delivery Driver", "Other")
            );

            roles.forEach(roleData -> {
                Role role = Role.builder()
                        .name(roleData.name)
                        .department(roleData.department)
                        .build();
                roleRepository.save(role);
            });

            System.out.println("✅ Roles seeded successfully!");
        } else {
            System.out.println("ℹ️ Roles already exist, skipping seed.");
        }
    }

    private static class RoleData {
        String name;
        String department;

        RoleData(String name, String department) {
            this.name = name;
            this.department = department;
        }
    }
}