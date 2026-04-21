package com.example.restaurantshifthandler.service;

import com.example.restaurantshifthandler.dto.CoverageRuleDTO;
import com.example.restaurantshifthandler.entity.CoverageRule;
import com.example.restaurantshifthandler.repository.CoverageRuleRepository;
import com.example.restaurantshifthandler.repository.RoleRepository;
import com.example.restaurantshifthandler.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoverageRuleService {

    private final CoverageRuleRepository repository;
    private final RoleRepository roleRepository;
    private final RestaurantRepository restaurantRepository;

    public List<CoverageRule> findAll() {
        return repository.findAll();
    }

    public Optional<CoverageRule> findById(Long id) {
        return repository.findById(id);
    }

    public List<CoverageRule> findByRestaurant(Long restaurantId) {
        return repository.findByRestaurantId(restaurantId);
    }

    public List<CoverageRule> findByRestaurantId(Long restaurantId) {
        return repository.findByRestaurantId(restaurantId);
    }

    public CoverageRule save(CoverageRuleDTO dto, Long restaurantId) {
        CoverageRule rule = CoverageRule.builder()
                .role(roleRepository.findById(dto.getRoleId())
                        .orElseThrow(() -> new RuntimeException("Role not found")))
                .restaurant(restaurantRepository.findById(restaurantId)
                        .orElseThrow(() -> new RuntimeException("Restaurant not found")))
                .minimumWorkers(dto.getMinimumWorkers())
                .build();

        return repository.save(rule);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public CoverageRule update(Long id, CoverageRuleDTO dto, Long restaurantId) {
        return repository.findById(id).map(rule -> {
            rule.setRole(roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found")));
            rule.setRestaurant(restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found")));
            rule.setMinimumWorkers(dto.getMinimumWorkers());

            return repository.save(rule);
        }).orElseThrow(() -> new RuntimeException("Coverage rule not found: " + id));
    }
}
