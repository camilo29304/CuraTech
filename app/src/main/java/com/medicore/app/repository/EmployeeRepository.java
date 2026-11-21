package com.medicore.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.medicore.app.models.Employee;

import jakarta.transaction.Transactional;


@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Employee findByDocumentNumber(String documentNumber);
    List<Employee> findByLastName(String lastName); 
    List<Employee> findByNameContainingIgnoreCase(String name);
    List<Employee> findByRoleTitleNameIgnoreCase(String roleName);
    @Modifying
    @Transactional
    int deleteByDocumentNumber(String documentNumber);


}
