package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Empleados;
import com.vitaNova.vitaNova.repository.EmpleadosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/view/empleados")
public class EmpleadosView {

    @Autowired
    private EmpleadosRepository empleadosRepository;


    // =========================================================
    // LISTA DE EMPLEADOS
    // =========================================================

    @GetMapping
    public String lista(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) String search) {

        // =====================================================
        // BUSCADOR
        // =====================================================

        if (search != null && !search.trim().isEmpty()) {

            model.addAttribute(
                    "empleados",
                    empleadosRepository.buscarEmpleados(
                            search.trim()
                    )
            );

        } else {

            model.addAttribute(
                    "empleados",
                    empleadosRepository.findAll()
            );
        }

        model.addAttribute(
                "search",
                search
        );

        model.addAttribute(
                "activeMenu",
                "empleados"
        );

        model.addAttribute(
                "userName",
                "Administrador"
        );

        model.addAttribute(
                "userRole",
                "Farmacia Central"
        );

        model.addAttribute(
                "pageTitle",
                "Gestión de Empleados"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y administración de los empleados de la farmacia."
        );

        model.addAttribute(
                "puedeModificar",
                puedeModificar(authentication)
        );

        return "empleados/empleados";
    }


    // =========================================================
    // EDITAR EMPLEADO
    // =========================================================

    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para editar empleados."
            );

            return "redirect:/view/empleados";
        }

        Empleados empleado = empleadosRepository
                .findById(id)
                .orElse(null);

        if (empleado == null) {

            ra.addFlashAttribute(
                    "error",
                    "Empleado no encontrado"
            );

            return "redirect:/view/empleados";
        }

        model.addAttribute(
                "empleado",
                empleado
        );

        model.addAttribute(
                "pageTitle",
                "Editar Empleado"
        );

        model.addAttribute(
                "pageSubtitle",
                "Actualice la información del empleado seleccionado."
        );

        model.addAttribute(
                "editMode",
                true
        );

        return "empleados/empleadosForm";
    }


    // =========================================================
    // ACTUALIZAR EMPLEADO
    // =========================================================

    @PostMapping("/save")
    public String save(
            @ModelAttribute Empleados empleado,
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para modificar empleados."
            );

            return "redirect:/view/empleados";
        }

        // Los empleados nuevos se registran desde Usuarios
        if (empleado.getId_empleado() == null) {

            ra.addFlashAttribute(
                    "success",
                    "Los empleados deben registrarse desde el módulo de Usuarios."
            );

            return "redirect:/view/usuarios/form";
        }

        Empleados empleadoAnterior = empleadosRepository
                .findById(empleado.getId_empleado())
                .orElse(null);

        if (empleadoAnterior == null) {

            ra.addFlashAttribute(
                    "error",
                    "Empleado no encontrado"
            );

            return "redirect:/view/empleados";
        }

        // =====================================================
        // CONSERVAR FECHA DE INGRESO ORIGINAL
        // =====================================================

        empleado.setFecha_ingreso(
                empleadoAnterior.getFecha_ingreso()
        );

        empleadosRepository.save(empleado);

        ra.addFlashAttribute(
                "success",
                "Empleado actualizado con éxito"
        );

        return "redirect:/view/empleados";
    }


    // =========================================================
    // ELIMINAR EMPLEADO
    // =========================================================

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para eliminar empleados."
            );

            return "redirect:/view/empleados";
        }

        if (empleadosRepository.existsById(id)) {

            empleadosRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Empleado eliminado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "error",
                    "Empleado no encontrado"
            );
        }

        return "redirect:/view/empleados";
    }


    // =========================================================
    // PERMISOS
    // =========================================================

    private boolean puedeModificar(
            Authentication authentication) {

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMINISTRADOR")
                                ||
                                authority.getAuthority()
                                        .equals("ROLE_FARMACEUTICO")
                );
    }
}