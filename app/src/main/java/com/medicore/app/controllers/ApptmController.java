package com.medicore.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.medicore.app.config.ScheduleTaskService;
import com.medicore.app.models.ApptmType;
import com.medicore.app.services.AppointmentService;



@Controller
@RequestMapping("/appointments")
public class ApptmController {
    @Autowired
    private AppointmentService appointmentService;
    @Autowired 
    private ScheduleTaskService taskService;
    
    @GetMapping("/types")
    public List<ApptmType> getAppointmentsTypes() {
        List<ApptmType> apptmTypes = appointmentService.getAppointmentsTypes();
        return apptmTypes;
    }

    @PostMapping("/create-spaces")
    public String createSpaces() {
        taskService.generateCurrentWeekAppointments();
        return "public/login";
    }
    

    @PostMapping("/schedule-apptm")
    public String scheduleApptm(@RequestParam String apptmTypeId, @RequestParam String doctorId, @RequestParam String date, @RequestParam String time, Model model) {
        appointmentService.scheduleAppointment(apptmTypeId, doctorId, date, time);    
        return "redirect:/patient/appointments/schedule";
    }
    
    @PostMapping("/cancel")
    public String cancelAppointment(@RequestParam int appointmentId) {
        appointmentService.cancelAppointment(appointmentId);
        return "redirect:/patient/appointments/cancel";
    }
    
    
}
