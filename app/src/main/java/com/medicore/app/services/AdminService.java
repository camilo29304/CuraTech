package com.medicore.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.medicore.app.models.Appointment;
import java.util.List;

import com.medicore.app.repository.AppointmentRepository;
import com.medicore.app.repository.EmployeeRepository;
import com.medicore.app.repository.PatientRepository;

@Service 
public class AdminService {
    @Autowired 
    EmployeeRepository employeeRepo;
    @Autowired 
    PatientRepository patientRepo;
    @Autowired 
    AppointmentRepository aptmRepo;

    public long getTotalEmployees(){
        return employeeRepo.count();
    }
    public long getTotalPatients(){
        return patientRepo.count();
    }
    public long getTotalAppointments(){
        return aptmRepo.findByIsAvailable(false).size();
    }
    public List<Appointment> getScheduledAppointments() {
        return aptmRepo.findByIsAvailable(false);
    }
}
