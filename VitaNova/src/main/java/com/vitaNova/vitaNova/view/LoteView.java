package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Lote;
import com.vitaNova.vitaNova.repository.LoteRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/view/lotes")
public class LoteView {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ProductosRepository productosRepository;

    // =========================
    // LISTA
    // Vista: lote/lote.html
    // =========================
    @GetMapping
    public String lista(Model model) {

        model.addAttribute(
                "lotes",
                loteRepository.findAll()
        );

        model.addAttribute(
                "activeMenu",
                "lotes"
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
                "Gestión de Lotes"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de los lotes de productos."
        );

        return "lote/lote";
    }

    // =========================
    // FORMULARIO NUEVO LOTE
    // =========================
    @GetMapping("/form")
    public String form(Model model) {

        Lote lote = new Lote();

        model.addAttribute(
                "lote",
                lote
        );

        model.addAttribute(
                "productos",
                productosRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Nuevo Lote"
        );

        model.addAttribute(
                "pageSubtitle",
                "Registre un nuevo lote para controlar la cantidad y vencimiento del producto."
        );

        model.addAttribute(
                "editMode",
                false
        );

        return "lote/loteForm";
    }

    // =========================
    // EDITAR LOTE
    // =========================
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Lote lote = loteRepository
                .findById(id)
                .orElse(null);

        if (lote == null) {

            ra.addFlashAttribute(
                    "success",
                    "Lote no encontrado"
            );

            return "redirect:/view/lotes";
        }

        model.addAttribute(
                "lote",
                lote
        );

        model.addAttribute(
                "productos",
                productosRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Editar Lote"
        );

        model.addAttribute(
                "pageSubtitle",
                "Actualice la información del lote seleccionado."
        );

        model.addAttribute(
                "editMode",
                true
        );

        return "lote/loteForm";
    }

    // =========================
    // GUARDAR / ACTUALIZAR
    // =========================
    @PostMapping("/save")
    public String save(
            @ModelAttribute Lote lote,
            Model model,
            RedirectAttributes ra) {

        boolean nuevo = lote.getId_lote() == null;

        // =========================
        // VALIDAR PRODUCTO
        // =========================
        if (lote.getId_producto() == null) {

            return mostrarError(
                    model,
                    lote,
                    nuevo,
                    "El producto es obligatorio"
            );
        }

        if (!productosRepository.existsById(lote.getId_producto())) {

            return mostrarError(
                    model,
                    lote,
                    nuevo,
                    "El producto seleccionado no existe"
            );
        }

        // =========================
        // VALIDAR NÚMERO DE LOTE
        // =========================
        if (lote.getNumero_lote() == null ||
                lote.getNumero_lote().trim().isEmpty()) {

            return mostrarError(
                    model,
                    lote,
                    nuevo,
                    "El número de lote es obligatorio"
            );
        }

        lote.setNumero_lote(
                lote.getNumero_lote().trim()
        );

        if (lote.getNumero_lote().length() > 50) {

            return mostrarError(
                    model,
                    lote,
                    nuevo,
                    "El número de lote no puede superar los 50 caracteres"
            );
        }

        // =========================
        // VALIDAR CANTIDAD
        // =========================
        if (lote.getCantidad() == null) {

            return mostrarError(
                    model,
                    lote,
                    nuevo,
                    "La cantidad es obligatoria"
            );
        }

        if (lote.getCantidad() <= 0) {

            return mostrarError(
                    model,
                    lote,
                    nuevo,
                    "La cantidad debe ser mayor que 0"
            );
        }

        // =========================
        // VALIDAR FECHA FABRICACIÓN
        // =========================
        if (lote.getFecha_fabricacion() == null) {

            return mostrarError(
                    model,
                    lote,
                    nuevo,
                    "La fecha de fabricación es obligatoria"
            );
        }

        // =========================
        // VALIDAR FECHA VENCIMIENTO
        // =========================
        if (lote.getFecha_vencimiento() == null) {

            return mostrarError(
                    model,
                    lote,
                    nuevo,
                    "La fecha de vencimiento es obligatoria"
            );
        }

        // =========================
        // VALIDAR ORDEN DE FECHAS
        // =========================
        if (lote.getFecha_vencimiento()
                .isBefore(lote.getFecha_fabricacion())) {

            return mostrarError(
                    model,
                    lote,
                    nuevo,
                    "La fecha de vencimiento no puede ser anterior a la fecha de fabricación"
            );
        }

        // =========================
        // VALIDAR ESTADO
        // =========================
        if (lote.getEstado() == null) {

            return mostrarError(
                    model,
                    lote,
                    nuevo,
                    "El estado es obligatorio"
            );
        }

        // =========================
        // VALIDAR LOTE ACTIVO VENCIDO
        // =========================
        if (lote.getEstado() == Lote.EstadoLote.ACTIVO &&
                lote.getFecha_vencimiento().isBefore(LocalDate.now())) {

            return mostrarError(
                    model,
                    lote,
                    nuevo,
                    "Un lote activo no puede tener una fecha de vencimiento pasada"
            );
        }

        // =========================
        // GUARDAR
        // =========================
        loteRepository.save(lote);

        if (nuevo) {

            ra.addFlashAttribute(
                    "success",
                    "Lote registrado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Lote actualizado con éxito"
            );
        }

        return "redirect:/view/lotes";
    }

    // =========================
    // MOSTRAR ERROR
    // =========================
    private String mostrarError(
            Model model,
            Lote lote,
            boolean nuevo,
            String mensaje) {

        model.addAttribute(
                "error",
                mensaje
        );

        model.addAttribute(
                "lote",
                lote
        );

        model.addAttribute(
                "productos",
                productosRepository.findAll()
        );

        if (nuevo) {

            model.addAttribute(
                    "pageTitle",
                    "Nuevo Lote"
            );

            model.addAttribute(
                    "pageSubtitle",
                    "Registre un nuevo lote para controlar la cantidad y vencimiento del producto."
            );

            model.addAttribute(
                    "editMode",
                    false
            );

        } else {

            model.addAttribute(
                    "pageTitle",
                    "Editar Lote"
            );

            model.addAttribute(
                    "pageSubtitle",
                    "Actualice la información del lote seleccionado."
            );

            model.addAttribute(
                    "editMode",
                    true
            );
        }

        return "lote/loteForm";
    }

    // =========================
    // ELIMINAR LOTE
    // =========================
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes ra) {

        if (loteRepository.existsById(id)) {

            loteRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Lote eliminado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Lote no encontrado"
            );
        }

        return "redirect:/view/lotes";
    }
}