package com.example.restaurantshifthandler.service;

import com.example.restaurantshifthandler.entity.Restaurant;
import com.example.restaurantshifthandler.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository repository;

    public List<Restaurant> findAll() {
        return repository.findAll();
    }

    public Optional<Restaurant> findById(Long id) {
        return repository.findById(id);
    }

    public Restaurant save(Restaurant restaurant) {
        return repository.save(restaurant);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public Restaurant update(Long id, Restaurant data) {
        return repository.findById(id).map(r -> {
            r.setName(data.getName());
            r.setAddress(data.getAddress());
            r.setPhone(data.getPhone());
            return repository.save(r);
        }).orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));
    }
}
