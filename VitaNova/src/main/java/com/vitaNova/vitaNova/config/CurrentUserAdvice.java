package com.vitaNova.vitaNova.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserAdvice {

    @ModelAttribute("usuarioActual")
    public String obtenerUsuarioActual(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "Usuario";
        }

        return authentication.getName();
    }

    @ModelAttribute("rolUsuarioActual")
    public String obtenerRolUsuarioActual(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "Usuario";
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {

            String rol = authority.getAuthority();

            if ("ROLE_ADMINISTRADOR".equals(rol)) {
                return "Administrador";
            }

            if ("ROLE_FARMACEUTICO".equals(rol)) {
                return "Farmacéutico";
            }

            if ("ROLE_VENDEDOR".equals(rol)) {
                return "Vendedor";
            }
        }

        return "Usuario";
    }
}