package com.UserSchedule.UserSchedule.repository;

import com.UserSchedule.UserSchedule.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    public Optional<User> findByUsername(String username);
    public Optional<User> findByEmail(String email);
    public Optional<User> findByKeycloakId(String keycloakId);
    public Optional<User> findByUserId(Integer id);
    public List<User> findByDepartment_DepartmentId(Integer departmentId);
    public List<User> findAllByKeycloakIdIn(Collection<String> keycloakIds);
}
