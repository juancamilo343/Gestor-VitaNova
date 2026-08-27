package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Proveedor;
import com.vitaNova.vitaNova.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/view/proveedores")
public class ProveedorView {

    @Autowired
    private ProveedorRepository proveedorRepository;

    // =========================================================
    // LISTA DE PROVEEDORES
    // =========================================================

    @GetMapping
    public String lista(Model model) {

        model.addAttribute(
                "proveedores",
                proveedorRepository.findAll()
        );

        model.addAttribute(
                "activeMenu",
                "proveedores"
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
                "Gestión de Proveedores"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de los proveedores de la farmacia."
        );

        return "proveedores/proveedores";
    }

    // =========================================================
    // NUEVO PROVEEDOR
    // =========================================================
    // Los proveedores nuevos se registran desde Usuarios.
    // =========================================================

    @GetMapping("/form")
    public String form(RedirectAttributes ra) {

        ra.addFlashAttribute(
                "success",
                "Los proveedores nuevos deben registrarse desde el módulo de Usuarios."
        );

        return "redirect:/view/usuarios/form";
    }

    // =========================================================
    // EDITAR PROVEEDOR
    // =========================================================

    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Proveedor proveedor = proveedorRepository
                .findById(id)
                .orElse(null);

        if (proveedor == null) {

            ra.addFlashAttribute(
                    "success",
                    "Proveedor no encontrado"
            );

            return "redirect:/view/proveedores";
        }

        model.addAttribute(
                "proveedor",
                proveedor
        );

        model.addAttribute(
                "pageTitle",
                "Editar Proveedor"
        );

        model.addAttribute(
                "pageSubtitle",
                "Actualice la información del proveedor seleccionado."
        );

        model.addAttribute(
                "editMode",
                true
        );

        return "proveedores/proveedoresForm";
    }

    // =========================================================
    // GUARDAR / ACTUALIZAR
    // =========================================================

    @PostMapping("/save")
    public String save(
            @ModelAttribute Proveedor proveedor,
            RedirectAttributes ra) {

        // Los proveedores nuevos deben registrarse desde Usuarios.
        if (proveedor.getId_proveedor() == null) {

            ra.addFlashAttribute(
                    "success",
                    "Los proveedores nuevos deben registrarse desde el módulo de Usuarios."
            );

            return "redirect:/view/usuarios/form";
        }

        if (!proveedorRepository.existsById(
                proveedor.getId_proveedor())) {

            ra.addFlashAttribute(
                    "success",
                    "Proveedor no encontrado"
            );

            return "redirect:/view/proveedores";
        }

        if (proveedor.getEstado() == null ||
                proveedor.getEstado().isBlank()) {

            proveedor.setEstado("ACTIVO");
        }

        proveedorRepository.save(proveedor);

        ra.addFlashAttribute(
                "success",
                "Proveedor actualizado con éxito"
        );

        return "redirect:/view/proveedores";
    }

    // =========================================================
    // ELIMINAR PROVEEDOR
    // =========================================================

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes ra) {

        if (proveedorRepository.existsById(id)) {

            proveedorRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Proveedor eliminado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Proveedor no encontrado"
            );
        }

        return "redirect:/view/proveedores";
    }
}