package com.UserSchedule.UserSchedule.repository;

import com.UserSchedule.UserSchedule.entity.Department;
import com.UserSchedule.UserSchedule.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    Optional<Department> findByName(String name);
    Optional<Department> findByDepartmentId(Integer id);
    @Query("SELECT u FROM User u WHERE u.department.departmentId = :departmentId")
    List<User> findUsersByDepartmentId(@Param("departmentId") Integer departmentId);
    List<Department> findAllByIsUsedTrue();

}
