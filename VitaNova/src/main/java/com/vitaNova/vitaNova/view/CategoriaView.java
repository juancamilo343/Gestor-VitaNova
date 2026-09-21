package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Categoria;
import com.vitaNova.vitaNova.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
    public String lista(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) String search) {

        // =====================================================
        // BUSCADOR
        // =====================================================

        if (search != null && !search.trim().isEmpty()) {

            model.addAttribute(
                    "categorias",
                    categoriaRepository.buscarCategorias(
                            search.trim()
                    )
            );

        } else {

            model.addAttribute(
                    "categorias",
                    categoriaRepository.findAll()
            );
        }

        model.addAttribute(
                "search",
                search
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

        // ADMINISTRADOR y FARMACÉUTICO pueden modificar
        model.addAttribute(
                "puedeModificar",
                puedeModificar(authentication)
        );

        return "categorias/categoria";
    }


    // =========================
    // FORMULARIO NUEVA CATEGORÍA
    // =========================
    @GetMapping("/form")
    public String form(
            Model model,
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para registrar categorías."
            );

            return "redirect:/view/categorias";
        }

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
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para editar categorías."
            );

            return "redirect:/view/categorias";
        }

        Categoria categoria = categoriaRepository
                .findById(id)
                .orElse(null);

        if (categoria == null) {

            ra.addFlashAttribute(
                    "error",
                    "Categoría no encontrada."
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
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para guardar categorías."
            );

            return "redirect:/view/categorias";
        }

        boolean nuevo =
                categoria.getId_categoria() == null;

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
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para eliminar categorías."
            );

            return "redirect:/view/categorias";
        }

        if (categoriaRepository.existsById(id)) {

            categoriaRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Categoría eliminada con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "error",
                    "Categoría no encontrada."
            );
        }

        return "redirect:/view/categorias";
    }


    // =========================
    // PERMISOS
    // ADMINISTRADOR
    // FARMACÉUTICO
    // =========================
    private boolean puedeModificar(
            Authentication authentication) {

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals(
                                "ROLE_ADMINISTRADOR"
                        )
                                ||
                                authority.getAuthority().equals(
                                        "ROLE_FARMACEUTICO"
                                )
                );
    }
}