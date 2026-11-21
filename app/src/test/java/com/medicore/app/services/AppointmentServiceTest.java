package com.medicore.app.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.medicore.app.models.Appointment;
import com.medicore.app.models.Patient;
import com.medicore.app.repository.AppointmentRepository;
import com.medicore.app.repository.ApptmTypeRepository;
import com.medicore.app.repository.PatientRepository;
import com.medicore.app.utils.UserSession;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repo;

    @Mock
    private ApptmTypeRepository apptmTypeRepo;

    @Mock
    private PatientRepository patientRepo;

    @InjectMocks
    private AppointmentService service;

    @Test
    void scheduleAppointment_marksAvailableAppointmentAsUnavailableAndAssignsPatient() {
        int appointmentId = 12;
        Patient patient = new Patient();
        Appointment appointment = new Appointment();
        appointment.setAvailable(true);
        when(repo.findById(appointmentId)).thenReturn(Optional.of(appointment));

        boolean scheduled = service.scheduleAppointment(appointmentId, patient);

        assertTrue(scheduled);
        assertFalse(appointment.isAvailable());
        assertSame(patient, appointment.getPatient());
        verify(repo).findById(appointmentId);
    }

    @Test
    void scheduleAppointment_returnsFalseWhenAppointmentIsUnavailable() {
        int appointmentId = 12;
        Patient currentPatient = new Patient();
        Patient requestedPatient = new Patient();
        Appointment appointment = new Appointment();
        appointment.setAvailable(false);
        appointment.setPatient(currentPatient);
        when(repo.findById(appointmentId)).thenReturn(Optional.of(appointment));

        boolean scheduled = service.scheduleAppointment(appointmentId, requestedPatient);

        assertFalse(scheduled);
        assertFalse(appointment.isAvailable());
        assertSame(currentPatient, appointment.getPatient());
        verify(repo).findById(appointmentId);
    }

    @Test
    void scheduleAppointment_throwsWhenAppointmentDoesNotExist() {
        int appointmentId = 404;
        when(repo.findById(appointmentId)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> service.scheduleAppointment(appointmentId, new Patient()));

        verify(repo).findById(appointmentId);
    }

    @Test
    void scheduleAppointment_withDateAndTimeAssignsPatientAndSavesAppointment() {
        String documentNumber = "12345678";
        int doctorId = 34;
        LocalDate date = LocalDate.of(2026, 12, 3);
        LocalTime time = LocalTime.of(10, 30);
        OffsetDateTime expectedDateTime = OffsetDateTime.of(
                date, LocalTime.of(5, 30), ZoneOffset.ofHours(-5));
        Patient patient = new Patient();
        Appointment appointment = new Appointment();
        UserSession.setDocumentNumber(documentNumber);
        when(repo.findByEmployeeIdAndDateTime(doctorId, expectedDateTime)).thenReturn(appointment);
        when(patientRepo.findByDocumentNumber(documentNumber)).thenReturn(Optional.of(patient));

        try {
            assertTrue(service.scheduleAppointment("7", String.valueOf(doctorId), date.toString(), time.toString()));

            assertSame(patient, appointment.getPatient());
            assertFalse(appointment.isAvailable());
            verify(repo).findByEmployeeIdAndDateTime(doctorId, expectedDateTime);
            verify(patientRepo).findByDocumentNumber(documentNumber);
            verify(repo).save(appointment);
        } finally {
            UserSession.setDocumentNumber(null);
        }
    }

    @Test
    void scheduleAppointment_withDateAndTimeThrowsWhenPatientDoesNotExist() {
        String documentNumber = "12345678";
        int doctorId = 34;
        LocalDate date = LocalDate.of(2026, 12, 3);
        LocalTime time = LocalTime.of(10, 30);
        OffsetDateTime expectedDateTime = OffsetDateTime.of(
                date, LocalTime.of(5, 30), ZoneOffset.ofHours(-5));
        Appointment appointment = new Appointment();
        UserSession.setDocumentNumber(documentNumber);
        when(repo.findByEmployeeIdAndDateTime(doctorId, expectedDateTime)).thenReturn(appointment);
        when(patientRepo.findByDocumentNumber(documentNumber)).thenReturn(Optional.empty());

        try {
            assertThrowsExactly(
                    EntityNotFoundException.class,
                    () -> service.scheduleAppointment("7", String.valueOf(doctorId), date.toString(), time.toString()));

            verify(repo).findByEmployeeIdAndDateTime(doctorId, expectedDateTime);
            verify(patientRepo).findByDocumentNumber(documentNumber);
        } finally {
            UserSession.setDocumentNumber(null);
        }
    }

    @Test
    void getAllAppointments_returnsAppointmentsFromRepository() {
        List<Appointment> appointments = List.of(new Appointment());
        when(repo.findAll()).thenReturn(appointments);

        assertSame(appointments, service.getAllAppointments());

        verify(repo).findAll();
    }

    @Test
    void formatDateTime_usesFiveHoursBehindUtc() {
        LocalDate date = LocalDate.of(2026, 12, 3);
        LocalTime time = LocalTime.of(10, 30);

        OffsetDateTime result = AppointmentService.formatDateTime(date, time);

        assertEquals(OffsetDateTime.of(date, time, ZoneOffset.ofHours(-5)), result);
    }

    @Test
    void rescheduleAppointment_goodCase(){
        Appointment toCancel = new Appointment();
        Patient patient = new Patient();
        toCancel.setAvailable(false);
        toCancel.setPatient(patient);

        Appointment toSchedule = new Appointment();
        toSchedule.setAvailable(true);


        assertTrue(service.rescheduleAppointment(toCancel, toSchedule));

        assertFalse(toSchedule.isAvailable());
        assertTrue(toCancel.isAvailable());
        assertNull(toCancel.getPatient());
        assertNotNull(toSchedule.getPatient());
        assertSame(patient, toSchedule.getPatient());

    }

    @Test
    void rescheduleAppointment_throwsWhenEitherAppointmentIsNull() {
        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> service.rescheduleAppointment(null, new Appointment()));
        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> service.rescheduleAppointment(new Appointment(), null));

        verifyNoInteractions(repo, apptmTypeRepo, patientRepo);
    }

    @Test
    void rescheduleAppointment_throwsWhenAppointmentToCancelHasNoPatient() {
        Appointment toCancel = new Appointment();
        toCancel.setAvailable(false);
        Appointment toSchedule = new Appointment();
        toSchedule.setAvailable(true);

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> service.rescheduleAppointment(toCancel, toSchedule));

        assertFalse(toCancel.isAvailable());
        assertNull(toCancel.getPatient());
        assertTrue(toSchedule.isAvailable());
        assertNull(toSchedule.getPatient());
    }

    @Test
    void rescheduleAppointment_throwsWhenDestinationAppointmentAlreadyHasPatient() {
        Appointment toCancel = new Appointment();
        Patient patientToMove = new Patient();
        toCancel.setAvailable(false);
        toCancel.setPatient(patientToMove);

        Appointment toSchedule = new Appointment();
        Patient existingPatient = new Patient();
        toSchedule.setAvailable(true);
        toSchedule.setPatient(existingPatient);

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> service.rescheduleAppointment(toCancel, toSchedule));

        assertFalse(toCancel.isAvailable());
        assertSame(patientToMove, toCancel.getPatient());
        assertTrue(toSchedule.isAvailable());
        assertSame(existingPatient, toSchedule.getPatient());
    }

    @Test
    void rescheduleAppointment_toCancelApptmIsAvailable(){
        Appointment toCancel = new Appointment();
        Patient patient = new Patient();
        toCancel.setAvailable(true);
        toCancel.setPatient(patient);

        Appointment toSchedule = new Appointment();
        toSchedule.setAvailable(true);


        assertThrows(IllegalStateException.class, () -> service.rescheduleAppointment(toCancel, toSchedule));
    
        assertTrue(toCancel.isAvailable());
        assertSame(patient, toCancel.getPatient());
        assertTrue(toSchedule.isAvailable());
        assertNull(toSchedule.getPatient());

    }

    @Test
    void rescheduleAppointment_throwsWhenDestinationAppointmentIsUnavailable() {
        Appointment toCancel = new Appointment();
        Patient patient = new Patient();
        toCancel.setAvailable(false);
        toCancel.setPatient(patient);
        Appointment toSchedule = new Appointment();
        toSchedule.setAvailable(false);

        assertThrowsExactly(
                IllegalStateException.class,
                () -> service.rescheduleAppointment(toCancel, toSchedule));

        assertFalse(toCancel.isAvailable());
        assertSame(patient, toCancel.getPatient());
        assertFalse(toSchedule.isAvailable());
        assertNull(toSchedule.getPatient());
    }

    @Test
    void cancelAppointment_releasesAppointmentAndSavesIt() {
        int appointmentId = 12;
        Patient patient = new Patient();
        Appointment appointment = new Appointment();
        appointment.setAvailable(false);
        appointment.setPatient(patient);
        when(repo.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertTrue(service.cancelAppointment(appointmentId));

        assertTrue(appointment.isAvailable());
        assertNull(appointment.getPatient());
        verify(repo).findById(appointmentId);
        verify(repo).save(appointment);
    }

    @Test
    void cancelAppointment_throwsWhenAppointmentDoesNotExist() {
        int appointmentId = 404;
        when(repo.findById(appointmentId)).thenReturn(Optional.empty());

        assertThrowsExactly(IllegalStateException.class, () -> service.cancelAppointment(appointmentId));

        verify(repo).findById(appointmentId);
    }

    @Test
    void cancelAppointment_throwsWhenAppointmentIsAlreadyAvailable() {
        int appointmentId = 12;
        Appointment appointment = new Appointment();
        appointment.setAvailable(true);
        when(repo.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrowsExactly(IllegalStateException.class, () -> service.cancelAppointment(appointmentId));

        assertTrue(appointment.isAvailable());
        assertNull(appointment.getPatient());
        verify(repo).findById(appointmentId);
    }

    @Test
    void cancelAppointment_throwsWhenOccupiedAppointmentHasNoPatient() {
        int appointmentId = 12;
        Appointment appointment = new Appointment();
        appointment.setAvailable(false);
        when(repo.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrowsExactly(IllegalStateException.class, () -> service.cancelAppointment(appointmentId));

        assertFalse(appointment.isAvailable());
        assertNull(appointment.getPatient());
        verify(repo).findById(appointmentId);
    }

    @Test
    void getNextAppointment_forDoctorUsesEmployeeQuery() {
        Patient person = new Patient();
        person.setDocumentNumber("12345678");
        Appointment appointment = new Appointment();
        when(repo.findNextApptmByEmployee("12345678")).thenReturn(appointment);

        assertSame(appointment, service.getNextAppointment(person, "MEDICO"));

        verify(repo).findNextApptmByEmployee("12345678");
    }

    @Test
    void getNextAppointment_forPatientUsesPatientQuery() {
        Patient person = new Patient();
        person.setDocumentNumber("12345678");
        Appointment appointment = new Appointment();
        when(repo.findNextApptmByPatient("12345678")).thenReturn(appointment);

        assertSame(appointment, service.getNextAppointment(person, "PACIENTE"));

        verify(repo).findNextApptmByPatient("12345678");
    }

    @Test
    void getNextAppointment_throwsForUnknownUserType() {
        Patient person = new Patient();

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> service.getNextAppointment(person, "ADMIN"));

        verifyNoInteractions(repo, apptmTypeRepo, patientRepo);
    }

    @Test
    void getAvailableAppointments_returnsAvailableAppointmentsFromRepository() {
        List<Appointment> appointments = List.of(new Appointment());
        when(repo.findByIsAvailable(true)).thenReturn(appointments);

        assertSame(appointments, service.getAvailableAppointments());

        verify(repo).findByIsAvailable(true);
    }

    @Test
    void getAvailableTimes_parsesIdsAndDateForRepositoryQuery() {
        LocalDate date = LocalDate.of(2026, 12, 3);
        List<Appointment> appointments = List.of(new Appointment());
        when(repo.findAppointmentsByDay(7, 34, date)).thenReturn(appointments);

        assertSame(appointments, service.getAvailableTimes("7", "34", date.toString()));

        verify(repo).findAppointmentsByDay(7, 34, date);
    }

}
