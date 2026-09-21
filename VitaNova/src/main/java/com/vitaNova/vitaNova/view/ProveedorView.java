package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Proveedor;
import com.vitaNova.vitaNova.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
    public String lista(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) String search) {

        if (search != null && !search.trim().isEmpty()) {

            model.addAttribute(
                    "proveedores",
                    proveedorRepository.buscarProveedores(search.trim())
            );

        } else {

            model.addAttribute(
                    "proveedores",
                    proveedorRepository.findAll()
            );
        }

        model.addAttribute(
                "search",
                search
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

        model.addAttribute(
                "puedeModificar",
                puedeModificar(authentication)
        );

        return "proveedores/proveedores";
    }

    // =========================================================
    // NUEVO PROVEEDOR
    // =========================================================
    // Los proveedores nuevos se registran desde Usuarios.
    // =========================================================

    @GetMapping("/form")
    public String form(
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para registrar proveedores."
            );

            return "redirect:/view/proveedores";
        }

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
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para editar proveedores."
            );

            return "redirect:/view/proveedores";
        }

        Proveedor proveedor = proveedorRepository
                .findById(id)
                .orElse(null);

        if (proveedor == null) {

            ra.addFlashAttribute(
                    "error",
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
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para modificar proveedores."
            );

            return "redirect:/view/proveedores";
        }

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
                    "error",
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
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para eliminar proveedores."
            );

            return "redirect:/view/proveedores";
        }

        if (proveedorRepository.existsById(id)) {

            proveedorRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Proveedor eliminado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "error",
                    "Proveedor no encontrado"
            );
        }

        return "redirect:/view/proveedores";
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