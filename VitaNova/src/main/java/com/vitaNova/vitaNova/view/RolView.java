package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Rol;
import com.vitaNova.vitaNova.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RolView {

    @Autowired
    private RolRepository rolRepository;


    // =========================================================
    // LISTADO DE ROLES
    // =========================================================

    @GetMapping("/view/rol")
    public String lista(
            Model model,
            @RequestParam(required = false) String error) {

        // Solo el ADMINISTRADOR puede acceder a la gestión de roles
        if (!puedeModificarRoles()) {
            return "redirect:/view/dashboard?error=sinPermisos";
        }

        model.addAttribute(
                "roles",
                rolRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Gestión de Roles"
        );

        model.addAttribute(
                "pageSubtitle",
                "Administre los roles, cargos y salarios de acceso del sistema."
        );

        model.addAttribute(
                "activeMenu",
                "roles"
        );

        // Permisos para mostrar/ocultar acciones
        model.addAttribute(
                "puedeModificar",
                puedeModificarRoles()
        );

        // Mensaje de error por falta de permisos
        if ("sinPermisos".equals(error)) {
            model.addAttribute(
                    "error",
                    "No tiene permisos para realizar esta acción."
            );
        }

        return "roles/rol";
    }


    // =========================================================
    // FORMULARIO NUEVO ROL
    // =========================================================

    @GetMapping("/view/rol/form")
    public String form(Model model) {

        // Solo el ADMINISTRADOR puede modificar roles
        if (!puedeModificarRoles()) {
            return "redirect:/view/rol?error=sinPermisos";
        }

        model.addAttribute(
                "roles",
                new Rol()
        );

        model.addAttribute(
                "pageTitle",
                "Nuevo Rol"
        );

        model.addAttribute(
                "pageSubtitle",
                "Configure la información, cargo y salario del nuevo rol."
        );

        model.addAttribute(
                "activeMenu",
                "roles"
        );

        return "roles/rolForm";
    }


    // =========================================================
    // GUARDAR / ACTUALIZAR ROL
    // =========================================================

    @PostMapping("/view/rol/save")
    public String save(
            @ModelAttribute("roles") Rol rol,
            RedirectAttributes ra) {

        // Protección backend
        if (!puedeModificarRoles()) {
            return "redirect:/view/rol?error=sinPermisos";
        }

        rolRepository.save(rol);

        ra.addFlashAttribute(
                "success",
                "Rol registrado exitosamente"
        );

        return "redirect:/view/rol";
    }


    // =========================================================
    // EDITAR ROL
    // =========================================================

    @GetMapping("/view/rol/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model) {

        // Protección backend
        if (!puedeModificarRoles()) {
            return "redirect:/view/rol?error=sinPermisos";
        }

        Rol rol = rolRepository
                .findById(id)
                .orElse(null);

        if (rol == null) {
            return "redirect:/view/rol";
        }

        model.addAttribute(
                "roles",
                rol
        );

        model.addAttribute(
                "pageTitle",
                "Editar Rol"
        );

        model.addAttribute(
                "pageSubtitle",
                "Configure la información, cargo y salario del rol."
        );

        model.addAttribute(
                "activeMenu",
                "roles"
        );

        return "roles/rolForm";
    }


    // =========================================================
    // ELIMINAR ROL
    // =========================================================

    @PostMapping("/view/rol/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes ra) {

        // Protección backend
        if (!puedeModificarRoles()) {
            return "redirect:/view/rol?error=sinPermisos";
        }

        rolRepository.deleteById(id);

        ra.addFlashAttribute(
                "success",
                "Rol eliminado exitosamente"
        );

        return "redirect:/view/rol";
    }


    // =========================================================
    // PERMISOS
    // =========================================================

    private boolean puedeModificarRoles() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMINISTRADOR")
                );
    }
}