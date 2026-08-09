package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Usuarios;
import com.vitaNova.vitaNova.repository.RolRepository;
import com.vitaNova.vitaNova.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/view/usuarios")
public class UsuariosView {

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private RolRepository rolRepository;

    // =========================
    // LISTA
    // Vista: usuarios/usuarios.html
    // =========================
    @GetMapping
    public String lista(Model model) {

        model.addAttribute(
                "usuarios",
                usuariosRepository.findAll()
        );

        // Datos generales del layout
        model.addAttribute(
                "activeMenu",
                "usuarios"
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
                "Gestión de Usuarios"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de usuarios del sistema."
        );

        return "usuarios/usuarios";
    }

    // =========================
    // FORMULARIO NUEVO USUARIO
    // =========================
    @GetMapping("/form")
    public String form(Model model) {

        Usuarios usuario = new Usuarios();

        // Por defecto el usuario queda activo
        usuario.setEstado(true);

        model.addAttribute(
                "usuario",
                usuario
        );

        model.addAttribute(
                "roles",
                rolRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Nuevo Usuario"
        );

        model.addAttribute(
                "pageSubtitle",
                "Registre y configure un nuevo usuario dentro del sistema VitaNova."
        );

        model.addAttribute(
                "editMode",
                false
        );

        return "usuarios/usuariosForm";
    }

    // =========================
    // EDITAR USUARIO
    // =========================
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Usuarios usuario = usuariosRepository
                .findById(id)
                .orElse(null);

        if (usuario == null) {

            ra.addFlashAttribute(
                    "success",
                    "Usuario no encontrado"
            );

            return "redirect:/view/usuarios";
        }

        model.addAttribute(
                "usuario",
                usuario
        );

        model.addAttribute(
                "roles",
                rolRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Editar Usuario"
        );

        model.addAttribute(
                "pageSubtitle",
                "Actualice la información del usuario seleccionado."
        );

        model.addAttribute(
                "editMode",
                true
        );

        return "usuarios/usuariosForm";
    }

    // =========================
    // GUARDAR / ACTUALIZAR
    // =========================
    @PostMapping("/save")
    public String save(
            @ModelAttribute Usuarios usuario,
            RedirectAttributes ra) {

        boolean nuevo = usuario.getId_usuario() == null;

        // Si no llega estado, se establece como activo
        if (usuario.getEstado() == null) {
            usuario.setEstado(true);
        }

        usuariosRepository.save(usuario);

        if (nuevo) {

            ra.addFlashAttribute(
                    "success",
                    "Usuario registrado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Usuario actualizado con éxito"
            );
        }

        return "redirect:/view/usuarios";
    }

    // =========================
    // ELIMINAR USUARIO
    // =========================
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes ra) {

        if (usuariosRepository.existsById(id)) {

            usuariosRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Usuario eliminado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Usuario no encontrado"
            );
        }

        return "redirect:/view/usuarios";
    }
}