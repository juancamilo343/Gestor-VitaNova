package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Empleados;
import com.vitaNova.vitaNova.repository.EmpleadosRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String lista(Model model) {

        model.addAttribute(
                "empleados",
                empleadosRepository.findAll()
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

        return "empleados/empleados";
    }

    // =========================================================
    // EDITAR EMPLEADO
    // =========================================================

    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Empleados empleado = empleadosRepository
                .findById(id)
                .orElse(null);

        if (empleado == null) {

            ra.addFlashAttribute(
                    "success",
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
            RedirectAttributes ra) {

        if (empleado.getId_empleado() == null) {

            ra.addFlashAttribute(
                    "success",
                    "Los empleados deben registrarse desde el módulo de Usuarios."
            );

            return "redirect:/view/empleados";
        }

        if (!empleadosRepository.existsById(
                empleado.getId_empleado())) {

            ra.addFlashAttribute(
                    "success",
                    "Empleado no encontrado"
            );

            return "redirect:/view/empleados";
        }

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
            RedirectAttributes ra) {

        if (empleadosRepository.existsById(id)) {

            empleadosRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Empleado eliminado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Empleado no encontrado"
            );
        }

        return "redirect:/view/empleados";
    }
}