package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Usuarios;
import com.vitaNova.vitaNova.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class UsuariosController {

    @Autowired
    private UsuariosRepository usuariosRepository;

    @ModelAttribute
    public void addActiveMenu(Model model) {
        model.addAttribute("activeMenu", "usuarios");
    }

    @GetMapping("/view/usuarios")
    public String lista(
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        List<Usuarios> usuarios;

        if (StringUtils.hasText(search)) {
            usuarios = usuariosRepository.search(search);
            model.addAttribute("search", search);
        } else {
            usuarios = usuariosRepository.findAll();
        }

        model.addAttribute("usuarios", usuarios);
        return "view/usuarios";
    }

    @GetMapping("/view/usuarios/form")
    public String form(Model model) {
        model.addAttribute("usuarios", new Usuarios());
        return "view/usuarios/form";
    }

    @PostMapping("/view/usuarios/save")
    public String save(
            @ModelAttribute Usuarios usuarios,
            RedirectAttributes ra) {

        boolean nuevo = usuarios.getId_usuario() == null;
        usuariosRepository.save(usuarios);

        ra.addFlashAttribute(
                "success",
                nuevo ? "Usuario registrado con éxito" : "Usuario actualizado con éxito"
        );

        return "redirect:/view/usuarios";
    }

    @GetMapping("/view/usuarios/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Usuarios usuario = usuariosRepository.findById(id).orElse(null);

        if (usuario == null) {
            ra.addFlashAttribute("success", "Usuario no encontrado");
            return "redirect:/view/usuarios";
        }

        model.addAttribute("usuarios", usuario);
        return "view/usuarios/form";
    }

    @PostMapping("/view/usuarios/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes ra) {

        if (usuariosRepository.existsById(id)) {
            usuariosRepository.deleteById(id);
            ra.addFlashAttribute("success", "Usuario eliminado con éxito");
        } else {
            ra.addFlashAttribute("success", "Usuario no encontrado");
        }

        return "redirect:/view/usuarios";
    }
}