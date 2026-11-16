package com.medicore.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.medicore.app.models.Appointment;
import com.medicore.app.services.AdminService;



@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired 
    AdminService adminService;

    @GetMapping
    public String showAdminMenu(Model model) {
        long totalEmployees = adminService.getTotalEmployees();
        long totalPatients = adminService.getTotalPatients();
        long totalAppointments = adminService.getTotalAppointments();
        List<Appointment> appointments = adminService.getScheduledAppointments();
        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("totalAppointments", totalAppointments);
        model.addAttribute("appointments", appointments);
        return "admin/admin-menu"; // Busca home.html en templates/
    }
    @GetMapping("/register-employees")
    public String showRegisterEmployeesView() {
        return "admin/register-employees"; // Busca registerEmployees.html en templates/
    }

    @GetMapping("/search-employees")
    public String showSearchEmployeesView() {
        return "admin/search-employees"; // Busca searchEmployees.html en templates/
    }

    @GetMapping("/medicine-stock")
    public String showMedicineStockView() {
        return "admin/medicine-stock"; // Busca medicinesStock.html en templates/
    }
    @GetMapping("/delete-users")
    public String showDeleteUsersView() {
        return "admin/delete-users"; // Busca deleteUsers.html en templates/
    }
}
