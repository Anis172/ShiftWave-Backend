package com.example.restaurantshifthandler.service;

import com.example.restaurantshifthandler.entity.Role;
import com.example.restaurantshifthandler.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository repository;

    public List<Role> findAll() {
        return repository.findAll();
    }

    public Optional<Role> findById(Long id) {
        return repository.findById(id);
    }

    public Role save(Role role) {
        return repository.save(role);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public Role update(Long id, Role data) {
        return repository.findById(id).map(r -> {
            r.setName(data.getName());
            r.setDepartment(data.getDepartment());
            return repository.save(r);
        }).orElseThrow(() -> new RuntimeException("Role not found: " + id));
    }
}