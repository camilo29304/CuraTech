package com.medicore.app.models;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BooleanSupplier;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointments")

public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; // database id
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "patient_id") //FK
    private Patient patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false) //FK 
    private Employee employee;

    @Column(name = "scheduled_at", nullable = false) 
    private OffsetDateTime dateTime;

    @Column(name="is_available")
    private boolean isAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="type")
    private ApptmType apptmType;
    

   
    public String getFormattedDateTime() {
        if (dateTime == null) return "";
        // Format: HH for 24h or hh for 12h
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

     public String getFormattedTime() {
        if (dateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return dateTime.format(formatter);
    }

    public String getFormattedDate() {
        if (dateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateTime.format(formatter);
    }

}