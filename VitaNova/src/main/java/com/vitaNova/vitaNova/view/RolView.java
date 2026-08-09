package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Rol;
import com.vitaNova.vitaNova.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RolView {

    @Autowired
    private RolRepository rolRepository;


    // =========================================================
    // LISTADO DE ROLES
    // =========================================================

    @GetMapping("/view/rol")
    public String lista(Model model) {

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
                "Administre los roles y permisos de acceso del sistema."
        );

        model.addAttribute(
                "activeMenu",
                "roles"
        );

        return "roles/rol";
    }


    // =========================================================
    // FORMULARIO NUEVO ROL
    // =========================================================

    @GetMapping("/view/rol/form")
    public String form(Model model) {

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
                "Configure la información del nuevo rol."
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
                "Configure la información del rol."
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

        rolRepository.deleteById(id);

        ra.addFlashAttribute(
                "success",
                "Rol eliminado exitosamente"
        );

        return "redirect:/view/rol";
    }
}