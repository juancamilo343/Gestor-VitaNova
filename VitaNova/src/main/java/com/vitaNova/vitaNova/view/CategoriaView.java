package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Categoria;
import com.vitaNova.vitaNova.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/view/categorias")
public class CategoriaView {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // =========================
    // LISTA
    // Vista: categorias/categoria.html
    // =========================
    @GetMapping
    public String lista(Model model) {

        model.addAttribute(
                "categorias",
                categoriaRepository.findAll()
        );

        model.addAttribute(
                "activeMenu",
                "categorias"
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
                "Gestión de Categorías"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de las categorías de productos."
        );

        return "categorias/categoria";
    }

    // =========================
    // FORMULARIO NUEVA CATEGORÍA
    // =========================
    @GetMapping("/form")
    public String form(Model model) {

        Categoria categoria = new Categoria();

        model.addAttribute(
                "categoria",
                categoria
        );

        model.addAttribute(
                "pageTitle",
                "Nueva Categoría"
        );

        model.addAttribute(
                "pageSubtitle",
                "Registre una nueva categoría para clasificar los productos de VitaNova."
        );

        model.addAttribute(
                "editMode",
                false
        );

        return "categorias/categoriaForm";
    }

    // =========================
    // EDITAR CATEGORÍA
    // =========================
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Categoria categoria = categoriaRepository
                .findById(id)
                .orElse(null);

        if (categoria == null) {

            ra.addFlashAttribute(
                    "success",
                    "Categoría no encontrada"
            );

            return "redirect:/view/categorias";
        }

        model.addAttribute(
                "categoria",
                categoria
        );

        model.addAttribute(
                "pageTitle",
                "Editar Categoría"
        );

        model.addAttribute(
                "pageSubtitle",
                "Actualice la información de la categoría seleccionada."
        );

        model.addAttribute(
                "editMode",
                true
        );

        return "categorias/categoriaForm";
    }

    // =========================
    // GUARDAR / ACTUALIZAR
    // =========================
    @PostMapping("/save")
    public String save(
            @ModelAttribute Categoria categoria,
            RedirectAttributes ra) {

        boolean nuevo = categoria.getId_categoria() == null;

        categoriaRepository.save(categoria);

        if (nuevo) {

            ra.addFlashAttribute(
                    "success",
                    "Categoría registrada con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Categoría actualizada con éxito"
            );
        }

        return "redirect:/view/categorias";
    }

    // =========================
    // ELIMINAR CATEGORÍA
    // =========================
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes ra) {

        if (categoriaRepository.existsById(id)) {

            categoriaRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Categoría eliminada con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Categoría no encontrada"
            );
        }

        return "redirect:/view/categorias";
    }
}