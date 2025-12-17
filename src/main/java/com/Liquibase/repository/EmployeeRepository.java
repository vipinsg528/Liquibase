package com.Liquibase.repository;

import com.Liquibase.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Basic CRUD methods like save(), findById(), findAll(), deleteById() are inherited.
}