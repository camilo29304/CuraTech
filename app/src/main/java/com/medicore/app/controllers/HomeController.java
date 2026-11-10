package com.medicore.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.medicore.app.services.LoginService;
import com.medicore.app.utils.UserSession;


@Controller
public class HomeController {
    @Autowired
    private LoginService loginService;

    @GetMapping("/")
    public String home() {
        return "public/rol-selection"; // Busca home.html en templates/
    }

    @GetMapping("/login")
    public String showLoginView(Model model) {
        model.addAttribute("role", UserSession.getRole());
        return "public/login"; // Busca home.html en templates/
    }

    @PostMapping("/set-role")
    public String setUsserSession(@RequestParam String role) {
        UserSession.setRole(role);
        System.out.println("Rol seleccionado: " + UserSession.getRole());
        return("redirect:/login");
    }

    @PostMapping("/login/validate")
    public String validateLogin(@RequestParam String documentNumber, @RequestParam String password) {
        String role = loginService.checkLogin(documentNumber, password);
        if (role == null) {
             System.out.println("Login fallido.");
            return "redirect:/login?error=true"; // Redirige de nuevo al login con un parámetro de error
        } 
        UserSession.setDocumentNumber(documentNumber);
        if (role.equals("ADMIN")) {
            return "redirect:/admin";
        } else if (role.equals("PACIENTE")) {
            return "redirect:/patient";
        } else if (role.equals("MEDICO")) {
            return "redirect:/employee";
        }
        return "redirect:/login?error=true";
    }
    
}