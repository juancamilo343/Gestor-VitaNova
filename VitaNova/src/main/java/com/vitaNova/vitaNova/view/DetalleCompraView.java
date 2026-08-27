package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Compra;
import com.vitaNova.vitaNova.model.DetalleCompra;
import com.vitaNova.vitaNova.repository.CompraRepository;
import com.vitaNova.vitaNova.repository.DetalleCompraRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/view/compras")
public class DetalleCompraView {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @Autowired
    private ProductosRepository productosRepository;


    // =====================================================
    // LISTAR DETALLES DE UNA COMPRA
    // =====================================================

    @GetMapping("/{id}/detalles")
    public String detalles(
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
                "detalles",
                detalleCompraRepository
                        .findAll()
                        .stream()
                        .filter(detalle ->
                                detalle.getId_compra() != null
                                        && detalle.getId_compra().equals(id)
                        )
                        .toList()
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
                "Detalle de Compra"
        );

        model.addAttribute(
                "pageSubtitle",
                "Productos incluidos en la compra seleccionada."
        );

        return "compras/detalleCompra";
    }


    // =====================================================
    // FORMULARIO NUEVO DETALLE
    // =====================================================

    @GetMapping("/{id}/detalles/form")
    public String form(
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

        DetalleCompra detalle = new DetalleCompra();

        detalle.setId_compra(id);
        detalle.setCantidad(1);
        detalle.setPrecio(BigDecimal.ZERO);
        detalle.setSubtotal(BigDecimal.ZERO);

        model.addAttribute(
                "compra",
                compra
        );

        model.addAttribute(
                "detalle",
                detalle
        );

        model.addAttribute(
                "productos",
                productosRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Agregar producto"
        );

        model.addAttribute(
                "pageSubtitle",
                "Agregue un producto a la compra seleccionada."
        );

        model.addAttribute(
                "editMode",
                false
        );

        model.addAttribute(
                "activeMenu",
                "compras"
        );

        return "compras/detalleCompraForm";
    }


    // =====================================================
    // EDITAR DETALLE
    // =====================================================

    @GetMapping("/{id}/detalles/edit/{detalleId}")
    public String edit(
            @PathVariable Long id,
            @PathVariable Long detalleId,
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

        DetalleCompra detalle =
                detalleCompraRepository
                        .findById(detalleId)
                        .orElse(null);

        if (detalle == null) {

            ra.addFlashAttribute(
                    "success",
                    "Detalle de compra no encontrado"
            );

            return "redirect:/view/compras/"
                    + id
                    + "/detalles";
        }

        model.addAttribute(
                "compra",
                compra
        );

        model.addAttribute(
                "detalle",
                detalle
        );

        model.addAttribute(
                "productos",
                productosRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Editar detalle"
        );

        model.addAttribute(
                "pageSubtitle",
                "Actualice la información del producto de la compra."
        );

        model.addAttribute(
                "editMode",
                true
        );

        model.addAttribute(
                "activeMenu",
                "compras"
        );

        return "compras/detalleCompraForm";
    }


    // =====================================================
    // GUARDAR DETALLE
    // =====================================================

    @PostMapping("/{id}/detalles/save")
    public String save(
            @PathVariable Long id,
            @ModelAttribute DetalleCompra detalle,
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

        // =================================================
        // ASIGNAR COMPRA
        // =================================================

        detalle.setId_compra(id);


        // =================================================
        // VALIDAR CANTIDAD
        // =================================================

        if (detalle.getCantidad() == null ||
                detalle.getCantidad() <= 0) {

            detalle.setCantidad(1);
        }


        // =================================================
        // VALIDAR PRECIO
        // =================================================

        if (detalle.getPrecio() == null) {

            detalle.setPrecio(
                    BigDecimal.ZERO
            );
        }


        // =================================================
        // CALCULAR SUBTOTAL
        // =================================================

        BigDecimal subtotal =
                detalle.getPrecio()
                        .multiply(
                                BigDecimal.valueOf(
                                        detalle.getCantidad()
                                )
                        );

        detalle.setSubtotal(subtotal);


        // =================================================
        // DETERMINAR SI ES NUEVO
        // =================================================

        boolean nuevo =
                detalle.getId_detalle_compra() == null;


        // =================================================
        // GUARDAR
        // =================================================

        detalleCompraRepository.save(detalle);


        // =================================================
        // RECALCULAR TOTAL DE LA COMPRA
        // =================================================

        BigDecimal total =
                detalleCompraRepository
                        .findAll()
                        .stream()
                        .filter(detalleItem ->
                                detalleItem.getId_compra() != null
                                        && detalleItem
                                        .getId_compra()
                                        .equals(id)
                        )
                        .map(DetalleCompra::getSubtotal)
                        .filter(subtotalItem ->
                                subtotalItem != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        compra.setTotal(total);

        compraRepository.save(compra);


        // =================================================
        // MENSAJE
        // =================================================

        if (nuevo) {

            ra.addFlashAttribute(
                    "success",
                    "Producto agregado a la compra"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Detalle actualizado correctamente"
            );
        }


        return "redirect:/view/compras/"
                + id
                + "/detalles";
    }


    // =====================================================
    // ELIMINAR DETALLE
    // =====================================================

    @PostMapping("/{id}/detalles/delete/{detalleId}")
    public String delete(
            @PathVariable Long id,
            @PathVariable Long detalleId,
            RedirectAttributes ra) {

        if (detalleCompraRepository.existsById(detalleId)) {

            detalleCompraRepository.deleteById(detalleId);


            // =================================================
            // BUSCAR COMPRA
            // =================================================

            Compra compra =
                    compraRepository
                            .findById(id)
                            .orElse(null);


            if (compra != null) {

                // =============================================
                // RECALCULAR TOTAL
                // =============================================

                BigDecimal total =
                        detalleCompraRepository
                                .findAll()
                                .stream()
                                .filter(detalle ->
                                        detalle.getId_compra() != null
                                                && detalle
                                                .getId_compra()
                                                .equals(id)
                                )
                                .map(DetalleCompra::getSubtotal)
                                .filter(subtotal ->
                                        subtotal != null
                                )
                                .reduce(
                                        BigDecimal.ZERO,
                                        BigDecimal::add
                                );

                compra.setTotal(total);

                compraRepository.save(compra);
            }


            ra.addFlashAttribute(
                    "success",
                    "Producto eliminado de la compra"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Detalle de compra no encontrado"
            );
        }


        return "redirect:/view/compras/"
                + id
                + "/detalles";
    }
}