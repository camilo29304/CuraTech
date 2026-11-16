package com.medicore.app.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.medicore.app.models.Employee;
import com.medicore.app.models.Role;
import com.medicore.app.models.Schedule;
import com.medicore.app.repository.EmployeeRepository;
import com.medicore.app.repository.RoleRepository;
import com.medicore.app.repository.ScheduleRepository;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private RoleRepository roleRepository;

    public List<Role> getRoles() {
        return roleRepository.findAll();
    }
    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    public List<Schedule> getSchedulesByEmployeeDocumentNumber(String documentNumber) {
        return scheduleRepository.findByEmployeeDocumentNumber(documentNumber);
    }

    public boolean deleteEmployeeByDocumentNumber(String documentNumber) {
        int deletedCount = employeeRepository.deleteByDocumentNumber(documentNumber);
        return deletedCount > 0;
    }

    public boolean saveEmployee(Employee employee){
        if (employeeRepository.findByDocumentNumber(employee.getDocumentNumber()) != null) {
            throw new IllegalArgumentException("El empleado ya existe");
        }
        employeeRepository.save(employee);
        return true;
    }

    public Employee getEmployeeByDocumentNumber(String documentNumber) {
        return employeeRepository.findByDocumentNumber(documentNumber);
    }

    public List<Employee> getEmployeeByLastName(String lastName) {
        return employeeRepository.findByLastName(lastName);
    }

}
