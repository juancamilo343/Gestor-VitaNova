package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Inventario;
import com.vitaNova.vitaNova.repository.InventarioRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/view/inventario")
public class InventarioView {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductosRepository productosRepository;

    // =========================
    // LISTA
    // =========================
    @GetMapping
    public String lista(Model model) {

        model.addAttribute(
                "inventarios",
                inventarioRepository.findAll()
        );

        model.addAttribute(
                "activeMenu",
                "inventario"
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
                "Gestión de Inventario"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento del inventario de la farmacia."
        );

        return "inventario/inventario";
    }

    // =========================
    // FORMULARIO NUEVO
    // =========================
    @GetMapping("/form")
    public String form(Model model) {

        Inventario inventario = new Inventario();

        inventario.setStock_actual(0);
        inventario.setStock_minimo(0);
        inventario.setStock_maximo(0);

        model.addAttribute(
                "inventario",
                inventario
        );

        model.addAttribute(
                "productos",
                productosRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Nuevo Inventario"
        );

        model.addAttribute(
                "pageSubtitle",
                "Registre el inventario de un producto existente."
        );

        model.addAttribute(
                "editMode",
                false
        );

        return "inventario/inventarioForm";
    }

    // =========================
    // EDITAR
    // =========================
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Inventario inventario = inventarioRepository
                .findById(id)
                .orElse(null);

        if (inventario == null) {

            ra.addFlashAttribute(
                    "success",
                    "Inventario no encontrado"
            );

            return "redirect:/view/inventario";
        }

        model.addAttribute(
                "inventario",
                inventario
        );

        model.addAttribute(
                "productos",
                productosRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Editar Inventario"
        );

        model.addAttribute(
                "pageSubtitle",
                "Actualice la información del inventario."
        );

        model.addAttribute(
                "editMode",
                true
        );

        return "inventario/inventarioForm";
    }

    // =========================
    // GUARDAR
    // =========================
    @PostMapping("/save")
    public String save(
            @ModelAttribute Inventario inventario,
            RedirectAttributes ra) {

        boolean nuevo =
                inventario.getId_inventario() == null;

        inventarioRepository.save(inventario);

        if (nuevo) {

            ra.addFlashAttribute(
                    "success",
                    "Inventario registrado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Inventario actualizado con éxito"
            );
        }

        return "redirect:/view/inventario";
    }

    // =========================
    // ELIMINAR
    // =========================
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes ra) {

        if (inventarioRepository.existsById(id)) {

            inventarioRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Inventario eliminado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Inventario no encontrado"
            );
        }

        return "redirect:/view/inventario";
    }
}