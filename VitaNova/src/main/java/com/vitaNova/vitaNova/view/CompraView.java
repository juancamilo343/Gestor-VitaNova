package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Compra;
import com.vitaNova.vitaNova.repository.CompraRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import com.vitaNova.vitaNova.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/view/compras")
public class CompraView {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ProductosRepository productosRepository;

    // =========================
    // LISTA
    // =========================
    @GetMapping
    public String lista(Model model) {

        model.addAttribute(
                "compras",
                compraRepository.findAll()
        );

        model.addAttribute(
                "activeMenu",
                "compras"
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
                "Gestión de Compras"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de las compras realizadas."
        );

        return "compras/compras";
    }

    // =========================
    // NUEVA COMPRA
    // =========================
    @GetMapping("/form")
    public String form(Model model) {

        Compra compra = new Compra();

        compra.setFecha(LocalDate.now());
        compra.setTotal(BigDecimal.ZERO);
        compra.setEstado("PENDIENTE");

        model.addAttribute(
                "compra",
                compra
        );

        model.addAttribute(
                "proveedores",
                proveedorRepository.findAll()
        );

        model.addAttribute(
                "productos",
                productosRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Nueva Compra"
        );

        model.addAttribute(
                "pageSubtitle",
                "Registre una nueva compra realizada a un proveedor."
        );

        model.addAttribute(
                "editMode",
                false
        );

        return "compras/comprasForm";
    }

    // =========================
    // EDITAR
    // =========================
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Compra compra = compraRepository
                .findById(id)
                .orElse(null);

        if (compra == null) {

            ra.addFlashAttribute(
                    "success",
                    "Compra no encontrada"
            );

            return "redirect:/view/compras";
        }

        model.addAttribute(
                "compra",
                compra
        );

        model.addAttribute(
                "proveedores",
                proveedorRepository.findAll()
        );

        model.addAttribute(
                "productos",
                productosRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Editar Compra"
        );

        model.addAttribute(
                "pageSubtitle",
                "Actualice la información de la compra."
        );

        model.addAttribute(
                "editMode",
                true
        );

        return "compras/comprasForm";
    }

    // =========================
    // GUARDAR
    // =========================
    @PostMapping("/save")
    public String save(
            @ModelAttribute Compra compra,
            RedirectAttributes ra) {

        boolean nuevo = compra.getId_compra() == null;

        if (compra.getEstado() == null ||
                compra.getEstado().isBlank()) {

            compra.setEstado("PENDIENTE");
        }

        if (compra.getTotal() == null) {
            compra.setTotal(BigDecimal.ZERO);
        }

        if (compra.getFecha() == null) {
            compra.setFecha(LocalDate.now());
        }

        compraRepository.save(compra);

        if (nuevo) {

            ra.addFlashAttribute(
                    "success",
                    "Compra registrada con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Compra actualizada con éxito"
            );
        }

        return "redirect:/view/compras";
    }

    // =========================
    // ELIMINAR
    // =========================
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes ra) {

        if (compraRepository.existsById(id)) {

            compraRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Compra eliminada con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Compra no encontrada"
            );
        }

        return "redirect:/view/compras";
    }
}