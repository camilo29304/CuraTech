package com.medicore.app.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.medicore.app.models.Appointment;
import com.medicore.app.models.ApptmType;
import com.medicore.app.models.Patient;
import com.medicore.app.models.Person;
import com.medicore.app.repository.AppointmentRepository;
import com.medicore.app.repository.ApptmTypeRepository;
import com.medicore.app.repository.PatientRepository;
import com.medicore.app.utils.UserSession;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository repo; 
    @Autowired
    private ApptmTypeRepository apptmTypeRepo;
    @Autowired
    private PatientRepository patientRepo;

    @Transactional
    public boolean scheduleAppointment(int idApptm, Patient patient) {
        Appointment apptm = repo.findById(idApptm).orElseThrow(() -> new EntityNotFoundException("Cita no encontrada con id: " + idApptm)); 
        
        if (apptm.isAvailable() == false) {
            System.out.println("la cita no está disponible");
            return false;
         }

         apptm.setAvailable(false);
         apptm.setPatient(patient);

         return true;
    }
    
    public boolean scheduleAppointment(String apptmTypeId, String doctorId, String date, String time){
        LocalDate capturedDate = LocalDate.parse(date);
        LocalTime capturedTime = LocalTime.parse(time);

        //ajustar las horas debido al uso de TimeStamp with TZ
        capturedTime = capturedTime.minusHours(5);
        OffsetDateTime dateTime = formatDateTime(capturedDate, capturedTime);

        Appointment toSchedule = repo.findByEmployeeIdAndDateTime(Integer.parseInt(doctorId), dateTime);
        Patient patient = patientRepo.findByDocumentNumber(UserSession.getDocumentNumber()).orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado con id: " + UserSession.getDocumentNumber()));
        toSchedule.setPatient(patient);
        toSchedule.setAvailable(false);
        repo.save(toSchedule);
        return true;
    }
    public List<Appointment> getAllAppointments() {
        return repo.findAll();
    }
    public List<ApptmType> getAppointmentsTypes() {
        return apptmTypeRepo.findAll();
    }

    public static OffsetDateTime formatDateTime(LocalDate capturedDate, LocalTime time) {      
        LocalDateTime date = capturedDate.atTime(time);
        OffsetDateTime dateTime = date.atOffset(ZoneOffset.of("-05:00"));
        
        return dateTime;
    }

    @Transactional
    public boolean rescheduleAppointment(Appointment toCancel, Appointment toSchedule){
        if (toCancel == null || toSchedule == null) {
            throw new IllegalArgumentException("Las citas no pueden ser nulas");
        }

        if (toCancel.isAvailable()) {
            throw new IllegalStateException("La cita a reprogramar no está ocupada");
        }
        if (!toSchedule.isAvailable()) {
            throw new IllegalStateException("La cita que desea no está disponible");
        }

        toCancel.setAvailable(true);
        toSchedule.setAvailable(false);

        toSchedule.setPatient(toCancel.getPatient());
        toCancel.setPatient(null);

        return true;
    }

    public boolean cancelAppointment(int idApptm){
        Appointment toCancel = repo.findById(idApptm).orElse(null);
        if (toCancel == null) {
            throw new IllegalStateException("La cita no fue encontrada");   
        }
        toCancel.setAvailable(true);
        toCancel.setPatient(null);
        repo.save(toCancel);
        return true;
    }

    public Appointment getNextAppointment(Person person, String userType){
        if (userType.equals("MEDICO")) {
            return repo.findNextApptmByEmployee(person.getDocumentNumber());
        }
        else if(userType.equals("PACIENTE")){
            return repo.findNextApptmByPatient(person.getDocumentNumber());
        }
        else{throw new IllegalArgumentException("Rol de usuario inválido");}
    }

    public List<Appointment> getAvailableAppointments() {
        return repo.findByIsAvailable(true);
    }

    public List<Appointment> getAppointmentsByPatient(Patient patient) {
        return repo.findByPatient_DocumentNumber(patient.getDocumentNumber());
    }

    public List<Appointment> getScheduledAppointmentsByDoctor() {
        return repo.findByIsAvailableAndEmployeeIsNotNull(false);
    }
    public List<Appointment> getAvailableTimes(String apptmTypeId, String doctorId, String date) {
        return repo.findAppointmentsByDay( Integer.parseInt(apptmTypeId), Integer.parseInt(doctorId), LocalDate.parse(date));
    }
    public List<Appointment> getActiveAppointmentsByPatient(Patient patient) {
        return repo.findByPatientDocumentNumberAndIsAvailable(patient.getDocumentNumber(), false);
    }
    
}
