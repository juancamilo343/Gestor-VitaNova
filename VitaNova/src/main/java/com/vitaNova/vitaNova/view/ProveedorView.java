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

    // =========================
    // LISTA
    // Vista: proveedores/proveedores.html
    // =========================
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

    // =========================
    // FORMULARIO NUEVO PROVEEDOR
    // =========================
    @GetMapping("/form")
    public String form(Model model) {

        Proveedor proveedor = new Proveedor();

        proveedor.setEstado("ACTIVO");

        model.addAttribute(
                "proveedor",
                proveedor
        );

        model.addAttribute(
                "pageTitle",
                "Nuevo Proveedor"
        );

        model.addAttribute(
                "pageSubtitle",
                "Registre un nuevo proveedor dentro del sistema VitaNova."
        );

        model.addAttribute(
                "editMode",
                false
        );

        return "proveedores/proveedoresForm";
    }

    // =========================
    // EDITAR PROVEEDOR
    // =========================
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

    // =========================
    // GUARDAR / ACTUALIZAR
    // =========================
    @PostMapping("/save")
    public String save(
            @ModelAttribute Proveedor proveedor,
            RedirectAttributes ra) {

        boolean nuevo = proveedor.getId_proveedor() == null;

        if (proveedor.getEstado() == null ||
                proveedor.getEstado().isBlank()) {

            proveedor.setEstado("ACTIVO");
        }

        proveedorRepository.save(proveedor);

        if (nuevo) {

            ra.addFlashAttribute(
                    "success",
                    "Proveedor registrado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Proveedor actualizado con éxito"
            );
        }

        return "redirect:/view/proveedores";
    }

    // =========================
    // ELIMINAR PROVEEDOR
    // =========================
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