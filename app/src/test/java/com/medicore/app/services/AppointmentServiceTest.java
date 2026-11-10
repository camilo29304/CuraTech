package com.medicore.app.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.medicore.app.models.Appointment;
import com.medicore.app.models.Patient;
import com.medicore.app.repository.AppointmentRepository;
import com.medicore.app.repository.ApptmTypeRepository;
import com.medicore.app.repository.PatientRepository;

@ExtendWith (MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repo; 
    @Mock
    private ApptmTypeRepository apptmTypeRepo;
    @Mock
    private PatientRepository patientRepo;

    @InjectMocks
    private AppointmentService appointmentService;

    Appointment apptm;
    int idApptm;
    Patient patient;

    @BeforeEach  
    void setUp(){
        apptm = new Appointment();
        idApptm = 1;
        patient = new Patient();
    }                                            

    @Test
    void testCancelScheduledAppointment() {
        
        apptm.setAvailable(false);
        apptm.setPatient(patient);
        when(repo.findById(idApptm)).thenReturn(Optional.of(apptm));


        boolean result = appointmentService.cancelAppointment(idApptm);

        assertTrue(result);
        assertTrue(apptm.isAvailable(), "La cita debería quedar marcada como disponible");
        assertNull(apptm.getPatient(), "El paciente asignado debería ser nulo");
        
        // Verificamos que repo.save(...) se haya ejecutado exactamente una vez con la cita modificada
        verify(repo, times(1)).save(apptm);
    }

    @Test
    void testCancelFreeAppointment() {
        apptm.setAvailable(true);
        
        when(repo.findById(idApptm)).thenReturn(Optional.of(apptm));


        IllegalStateException exception = assertThrows(IllegalStateException.class, 
                                        () -> appointmentService.cancelAppointment(idApptm));

        
        assertEquals("La cita no se puede cancelar porque ya está disponible", exception.getMessage());
    
        // 3. Verificamos que NUNCA intente guardar nada en la base de datos
        verify(repo, never()).save(any());
        
    }

    @Test
    void testCancelPatientLessAppointment() {
        apptm.setAvailable(false);
        
        when(repo.findById(idApptm)).thenReturn(Optional.of(apptm));


        IllegalStateException exception = assertThrows(IllegalStateException.class, 
                                        () -> appointmentService.cancelAppointment(idApptm));

        
        assertEquals("La cita debe tener un paciente asociado", exception.getMessage());
    
        // 3. Verificamos que NUNCA intente guardar nada en la base de datos
        verify(repo, never()).save(any());
        
    }

    @Test
    void testFormatDateTime() {
        LocalDate date = LocalDate.of(2026, 10, 8); // 8 de Octubre de 2026
        LocalTime hour = LocalTime.of(14, 30);      // 02:30 PM (formato 24h)

        OffsetDateTime result = appointmentService.formatDateTime(date, hour);
        OffsetDateTime expected = OffsetDateTime.of(2026, 10, 8, 14, 30, 0, 0, ZoneOffset.of("-05:00"));

        assertEquals(expected, result);
    }

    @Test
    void testRescheduleAppointment() {
        Appointment toCancel = new Appointment();
        toCancel.setPatient(patient);
        toCancel.setAvailable(false);

        Appointment toSchedule = new Appointment();
        toSchedule.setAvailable(true);

        boolean result = appointmentService.rescheduleAppointment(toCancel, toSchedule);

        assertTrue(result);
        assertNull(toCancel.getPatient());
        assertEquals(patient, toSchedule.getPatient());
        assertEquals(true, toCancel.isAvailable());
        assertEquals(false, toSchedule.isAvailable());
    }

    @Test
    void testScheduleAppointment() {

    }

    @Test
    void testScheduleAppointment2() {

    }
}
