package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Rol;
import com.vitaNova.vitaNova.repository.RolRepository;
import com.vitaNova.vitaNova.service.RolPermisosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class RolView {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private RolPermisosService rolPermisosService;


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

        List<Rol> roles = rolRepository.findAll();
        Map<Long, Set<String>> permisosPorRol = new HashMap<>();
        for (Rol r : roles) {
            permisosPorRol.put(r.getId_rol(), rolPermisosService.obtenerPermisos(r.getNombre(), r.getId_rol()));
        }

        model.addAttribute("roles", roles);
        model.addAttribute("permisosPorRol", permisosPorRol);
        model.addAttribute("catalogoPermisos", rolPermisosService.getCatalogoPermisos());

        model.addAttribute(
                "pageTitle",
                "Gestión de Roles"
        );

        model.addAttribute(
                "pageSubtitle",
                "Administre los roles, permisos parametrizables y salarios del sistema."
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

        return "Roles/rol";
    }


    // =========================================================
    // FORMULARIO NUEVO ROL
    // =========================================================

    @GetMapping("/view/rol/form")
    public String form(Model model) {

        if (!puedeModificarRoles()) {
            return "redirect:/view/rol?error=sinPermisos";
        }

        model.addAttribute(
                "roles",
                new Rol()
        );

        model.addAttribute("catalogoPermisos", rolPermisosService.getCatalogoPermisos());
        model.addAttribute("permisosAsignados", Collections.emptySet());

        model.addAttribute(
                "pageTitle",
                "Nuevo Rol"
        );

        model.addAttribute(
                "pageSubtitle",
                "Configure la información, cargo, salario y permisos del nuevo rol."
        );

        model.addAttribute(
                "activeMenu",
                "roles"
        );

        return "Roles/rolForm";
    }


    // =========================================================
    // GUARDAR / ACTUALIZAR ROL CON PERMISOS
    // =========================================================

    @PostMapping("/view/rol/save")
    public String save(
            @ModelAttribute("roles") Rol rol,
            @RequestParam(name = "permisos", required = false) List<String> permisos,
            RedirectAttributes ra) {

        if (!puedeModificarRoles()) {
            return "redirect:/view/rol?error=sinPermisos";
        }

        Rol guardado = rolRepository.save(rol);
        rolPermisosService.guardarPermisosParaRol(guardado.getId_rol(), guardado.getNombre(), permisos);

        ra.addFlashAttribute(
                "success",
                "Rol y permisos guardados exitosamente."
        );

        return "redirect:/view/rol";
    }


    // =========================================================
    // EDITAR ROL Y PERMISOS
    // =========================================================

    @GetMapping("/view/rol/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model) {

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

        model.addAttribute("catalogoPermisos", rolPermisosService.getCatalogoPermisos());
        model.addAttribute("permisosAsignados", rolPermisosService.obtenerPermisos(rol.getNombre(), rol.getId_rol()));

        model.addAttribute(
                "pageTitle",
                "Editar Rol y Permisos"
        );

        model.addAttribute(
                "pageSubtitle",
                "Parametrice los permisos de acceso y funciones del rol."
        );

        model.addAttribute(
                "activeMenu",
                "roles"
        );

        return "Roles/rolForm";
    }


    // =========================================================
    // ELIMINAR ROL
    // =========================================================

    @PostMapping("/view/rol/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes ra) {

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
