package com.medicore.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.medicore.app.models.Prescription;
import com.medicore.app.services.MedicineService;
import com.medicore.app.services.PrescriptionService;

@Controller
@RequestMapping("/prescription")
public class PrescriptionController {
    @Autowired
    private PrescriptionService prescriptionService;
    @Autowired
    private MedicineService medicineService;

    

    @PostMapping("/save")
    public String savePrescription(@ModelAttribute Prescription prescription, BindingResult bindingResult ,Model model ) {
        if(bindingResult.hasErrors()){
            return "employee/create-prescription";
        }
        
        if (!medicineService.isAvailable(prescription)) {
             bindingResult.rejectValue("batchStock", "error.Stock", "Sin stock disponible para este medicamento");
             return "employee/create-prescription";
        }

        medicineService.setBatchStock(prescription);
        prescription.setActive(true);
        prescriptionService.savePrescription(prescription);
        return "redirect:/employee/create-prescription-view";
               
    }
}
