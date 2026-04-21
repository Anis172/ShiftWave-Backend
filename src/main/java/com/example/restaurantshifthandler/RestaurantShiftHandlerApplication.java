package com.example.restaurantshifthandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RestaurantShiftHandlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantShiftHandlerApplication.class, args);
    }

}
