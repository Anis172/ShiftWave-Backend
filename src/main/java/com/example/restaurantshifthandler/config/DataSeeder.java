package com.example.restaurantshifthandler.config;

import com.example.restaurantshifthandler.entity.Role;
import com.example.restaurantshifthandler.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
            roleRepository.saveAll(List.of(
                    Role.builder().name("Head Chef").department("Kitchen").build(),
                    Role.builder().name("Sous Chef").department("Kitchen").build(),
                    Role.builder().name("Line Cook").department("Kitchen").build(),
                    Role.builder().name("Prep Cook").department("Kitchen").build(),
                    Role.builder().name("Dishwasher").department("Kitchen").build(),
                    Role.builder().name("Waiter/Server").department("Floor").build(),
                    Role.builder().name("Host/Hostess").department("Floor").build(),
                    Role.builder().name("Busboy").department("Floor").build(),
                    Role.builder().name("Bartender").department("Bar").build(),
                    Role.builder().name("Barback").department("Bar").build(),
                    Role.builder().name("Manager").department("Management").build(),
                    Role.builder().name("Shift Supervisor").department("Management").build(),
                    Role.builder().name("Cashier").department("Other").build(),
                    Role.builder().name("Delivery Driver").department("Other").build()
            ));
            System.out.println("Roles seeded successfully!");
        } else {
            System.out.println("Roles already exist, skipping seed.");
        }
    }
}