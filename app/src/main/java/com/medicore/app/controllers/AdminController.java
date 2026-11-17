package com.medicore.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.medicore.app.models.Appointment;
import com.medicore.app.models.BatchStock;
import com.medicore.app.models.Employee;
import com.medicore.app.models.Medicine;
import com.medicore.app.services.AdminService;
import com.medicore.app.repository.RoleRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired 
    AdminService adminService;
    @Autowired
    RoleRepository roleRepository;

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
    public String showRegisterEmployeesView(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/register-employees"; // Busca registerEmployees.html en templates/
    }

    @PostMapping("/register-employees")
    public String registerEmployee(@ModelAttribute Employee employee,
                                    @RequestParam("roleName") String roleName,
                                    RedirectAttributes redirectAttributes) {
        try {
            adminService.registerEmployee(employee, roleName);
            redirectAttributes.addFlashAttribute("success", "Empleado registrado correctamente");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/register-employees";
    }

    @GetMapping("/search-employees")
    public String showSearchEmployeesView(@RequestParam(required = false, defaultValue = "") String query,
                                          @RequestParam(required = false, defaultValue = "") String role,
                                          @RequestParam(required = false, defaultValue = "") String status,
                                          Model model) {
        model.addAttribute("employees", adminService.searchEmployees(query, role, status));
        model.addAttribute("query", query);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        return "admin/search-employees"; // Busca searchEmployees.html en templates/
    }

    @GetMapping("/medicine-stock")
    public String showMedicineStockView(@RequestParam(required = false, defaultValue = "") String query,
                                        @RequestParam(required = false, defaultValue = "") String category,
                                        @RequestParam(required = false, defaultValue = "") String status,
                                        Model model) {
        model.addAttribute("stock", adminService.searchStock(query, category, status));
        model.addAttribute("activeBatches", adminService.getActiveBatchCount());
        model.addAttribute("lowStock", adminService.getLowStockCount());
        model.addAttribute("expiringSoon", adminService.getExpiringSoonCount());
        model.addAttribute("query", query);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        return "admin/medicine-stock"; // Busca medicinesStock.html en templates/
    }

    @PostMapping("/medicine-stock/register")
    public String registerMedicine(@ModelAttribute Medicine medicine,
                                   RedirectAttributes redirectAttributes) {
        try {
            adminService.registerMedicine(medicine);
            redirectAttributes.addFlashAttribute("success", "Medicamento registrado correctamente");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/medicine-stock";
    }

    @PostMapping("/medicine-stock/{id}/update")
    public String updateMedicine(@PathVariable int id,
                                 @ModelAttribute Medicine medicine,
                                 RedirectAttributes redirectAttributes) {
        try {
            adminService.updateMedicine(id, medicine);
            redirectAttributes.addFlashAttribute("success", "Medicamento actualizado correctamente");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/medicine-stock";
    }

    @PostMapping("/medicine-stock/{id}/adjust")
    public String adjustStock(@PathVariable int id,
                              @RequestParam int quantity,
                              RedirectAttributes redirectAttributes) {
        try {
            adminService.adjustStock(id, quantity);
            redirectAttributes.addFlashAttribute("success", "Stock actualizado correctamente");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/medicine-stock";
    }

    @GetMapping("/delete-users")
    public String showDeleteUsersView(@RequestParam(required = false, defaultValue = "") String query,
                                      @RequestParam(required = false, defaultValue = "") String role,
                                      Model model) {
        model.addAttribute("users", adminService.getEligibleUsers(query, role));
        model.addAttribute("query", query);
        model.addAttribute("selectedRole", role);
        return "admin/delete-users"; // Busca deleteUsers.html en templates/
    }

    @PostMapping("/employees/{id}/activate")
    public String activateEmployee(@PathVariable int id,
                                   RedirectAttributes redirectAttributes) {
        if (!adminService.activateEmployee(id)) {
            redirectAttributes.addFlashAttribute("error", "Empleado no encontrado");
        }
        return "redirect:/admin/search-employees";
    }

    @PostMapping("/employees/{id}/deactivate")
    public String deactivateEmployee(@PathVariable int id,
                                     RedirectAttributes redirectAttributes) {
        if (!adminService.deactivateEmployee(id)) {
            redirectAttributes.addFlashAttribute("error", "Empleado no encontrado");
        }
        return "redirect:/admin/search-employees";
    }

    @PostMapping("/employees/{id}/update")
    public String updateEmployee(@PathVariable int id,
                                 @ModelAttribute Employee employee,
                                 RedirectAttributes redirectAttributes) {
        try {
            adminService.updateEmployee(id, employee);
            redirectAttributes.addFlashAttribute("success", "Empleado actualizado correctamente");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/search-employees";
    }

    @PostMapping("/usuarios/{documentNumber}/inactivar")
    public String inactivateUser(@PathVariable String documentNumber,
                                 RedirectAttributes redirectAttributes) {
        if (!adminService.inactivateUser(documentNumber)) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
        }
        return "redirect:/admin/delete-users";
    }

    @PostMapping("/usuarios/{documentNumber}/activar")
    public String activateUser(@PathVariable String documentNumber,
                               RedirectAttributes redirectAttributes) {
        if (!adminService.activateUser(documentNumber)) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
        }
        return "redirect:/admin/delete-users";
    }

    @PostMapping("/usuarios/eliminar")
    public String deleteUser(@RequestParam("usuarioId") String usuarioId,
                             RedirectAttributes redirectAttributes) {
        if (!adminService.deleteUser(usuarioId)) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
        } else {
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado correctamente");
        }
        return "redirect:/admin/delete-users";
    }
}
