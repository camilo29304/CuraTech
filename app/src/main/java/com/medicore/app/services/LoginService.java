package com.medicore.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.medicore.app.models.User;
import com.medicore.app.repository.UserRepository;
import com.medicore.app.utils.UserSession;

@Service
public class LoginService {
    @Autowired
    private UserRepository userRepository;
    
    public String checkLogin(String documentNumber, String password) {
        User user = userRepository.findByDocumentNumberAndPassword(documentNumber, password);
        if(user == null) {
            throw new IllegalArgumentException("Usuario no encontrado o contraseña incorrecta");
        }
        if(user.getRole().equals(UserSession.getRole())){
            return UserSession.getRole();
        }
        else if(user.getRole().equals("ADMIN")){
            return UserSession.getRole();
        }
        else {
            throw new IllegalStateException("El rol seleccionado no coincide con el rol del usuario");
        }

    }
    //return data can be changed according to used data base
    public String getEntryDocument() throws IllegalAccessException{
        String role = UserSession.getRole();
        if (role.equals("PACIENTE")) {
            return "1000123456";
        }
        else if(role.equals("MEDICO")){
            return "1020456789";
        }
        else if(role.equals("ADMIN")){
            return "1234567890";
        }
        else{throw new IllegalAccessException("Rol invalido");}

    }

    public String getEntryPassword() throws IllegalAccessException{
        String role = UserSession.getRole();
        if (role.equals("PACIENTE") || role.equals("MEDICO")) {
            return "pass123";
        }
        else if(role.equals("ADMIN")){
            return "admin123";
        }
        else{throw new IllegalAccessException("Rol invalido");}

    }

}
