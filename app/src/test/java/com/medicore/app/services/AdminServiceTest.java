package com.medicore.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.medicore.app.models.Employee;
import com.medicore.app.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private EmployeeRepository employeeRepo;

    @InjectMocks
    private AdminService service;

    @Test
    void registerEmployee_rejectsEmployeeYoungerThanEighteen() {
        Employee employee = new Employee();
        employee.setBirthDate(LocalDate.now().minusYears(18).plusDays(1));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEmployee(employee, "Médico"));

        assertEquals("El empleado debe tener al menos 18 años", exception.getMessage());
        verifyNoInteractions(employeeRepo);
    }
}
