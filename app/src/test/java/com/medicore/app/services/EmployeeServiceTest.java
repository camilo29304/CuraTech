package com.medicore.app.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.medicore.app.models.Employee;
import com.medicore.app.repository.EmployeeRepository;
import com.medicore.app.repository.RoleRepository;
import com.medicore.app.repository.ScheduleRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock 
    EmployeeRepository employeeRepo;
    @Mock 
    ScheduleRepository scheduleRepo;
    @Mock 
    RoleRepository roleRepo;

    @InjectMocks 
    EmployeeService service;

    @Test 
    void saveEmployee(){
        Employee employee = new Employee();
        employee.setDocumentNumber("123456789");

        when(employeeRepo.findByDocumentNumber(employee.getDocumentNumber())).thenReturn(null);

        boolean result = service.saveEmployee(employee);
        
        verify(employeeRepo).save(employee);
        assertTrue(result);
    }

    @Test 
    void saveEmployee_employeeAlreadyExist(){
        Employee employee = new Employee();
        employee.setDocumentNumber("123456789");

        when(employeeRepo.findByDocumentNumber(employee.getDocumentNumber())).thenReturn(employee);

        assertThrows( IllegalArgumentException.class , () -> service.saveEmployee(employee));
        verify(employeeRepo, never()).save(employee);
    }
}
