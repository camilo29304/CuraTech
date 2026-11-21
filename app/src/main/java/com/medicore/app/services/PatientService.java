package com.medicore.app.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.medicore.app.models.Patient;
import com.medicore.app.models.Pqrs;
import com.medicore.app.repository.PatientRepository;
import com.medicore.app.repository.PqrsRepository;

@Service
public class PatientService {
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private PqrsRepository pqrsRepository;

    public boolean savePatient(Patient patient){
        if(patientRepository.findByDocumentNumber(patient.getDocumentNumber()).isPresent()){
            throw new IllegalArgumentException("El paciente ya existe");
        }
        patientRepository.save(patient);
        return true;
    }
    public boolean updatePatient(Patient patient){
        if(!patientRepository.findByDocumentNumber(patient.getDocumentNumber()).isPresent()){
            throw new IllegalArgumentException("El paciente no existe");
        }
        patientRepository.save(patient);
        return true;
    }
    public boolean deletePatient(String documentNumber){
        if(!patientRepository.findByDocumentNumber(documentNumber).isPresent()){
           throw new IllegalArgumentException("El paciente no existe");
        }
        patientRepository.deleteByDocumentNumber(documentNumber);
        return true;
    }
    public Patient getPatientByDocumentNumber(String documentNumber){
        return patientRepository.findByDocumentNumber(documentNumber).orElse(null);
    }

    public List<Patient> getPatientByLastName(String lastName){
        return patientRepository.findByLastName(lastName);
    }

    public boolean savePqrs(Pqrs pqrs){
        pqrsRepository.save(pqrs);
        return true;
    }
    public List<Pqrs> getPqrsByPatient(String documentNumber) {
        return pqrsRepository.findByPatientDocumentNumber(documentNumber);
       
    }
    public List<Patient> searchPatientsByName(String name) {
        return patientRepository.findByNameContainingIgnoreCase(name);
    }
    public Optional<Patient> searchPatientsByDocument(String document) {
        return patientRepository.findByDocumentNumber(document);
    }
}
