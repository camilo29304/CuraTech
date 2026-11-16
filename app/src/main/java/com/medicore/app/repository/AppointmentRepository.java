package com.medicore.app.repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.medicore.app.models.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer>{

    boolean existsByDateTime(OffsetDateTime dateTime);
    List<Appointment> findByPatient_DocumentNumber(String documentNumber);
    List<Appointment> findByDateTimeAndPatient_DocumentNumber(OffsetDateTime dateTime, String documentNumber);
    Appointment findByIsAvailableAndDateTime(boolean available, OffsetDateTime dateTime);
    List<Appointment> findByIsAvailable(boolean available);
    List<Appointment> findByIsAvailableAndEmployeeIsNotNull(boolean available);
    
    @Query("SELECT a FROM Appointment a " +
       "WHERE a.isAvailable = true " +
       "AND a.apptmType.id = :apptmTypeId " +
       "AND a.employee.id = :employeeId " +
       "AND DATE(a.dateTime) = :date")
    List<Appointment> findAppointmentsByDay(
        @Param("apptmTypeId") int apptmTypeId,
        @Param("employeeId") int employeeId,
        @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a " +
            "JOIN a.employee e " +
            "WHERE e.documentNumber = :documentNumber AND  a.dateTime > CURRENT_TIMESTAMP " +
            "ORDER BY a.dateTime ASC "+
            "LIMIT 1")
    Appointment findNextApptmByEmployee( @Param("documentNumber") String documentNumber);

    @Query("SELECT a FROM Appointment a " +
            "JOIN a.patient p " +
            "WHERE p.documentNumber = :documentNumber AND  a.dateTime > CURRENT_TIMESTAMP " +
            "ORDER BY a.dateTime ASC "+
            "LIMIT 1")
    Appointment findNextApptmByPatient( @Param("documentNumber") String documentNumber);

    Appointment findByEmployeeIdAndDateTime(int employeeId, OffsetDateTime dateTime);
    List<Appointment> findByPatientDocumentNumberAndIsAvailable(String documentNumber, boolean isAvailable);
    boolean existsByDateTimeGreaterThanEqualAndDateTimeLessThan(OffsetDateTime weekStart, OffsetDateTime nextWeekStart);
    
}
