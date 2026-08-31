package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.DetalleVenta;
import com.vitaNova.vitaNova.model.Venta;
import com.vitaNova.vitaNova.repository.DetalleVentaRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import com.vitaNova.vitaNova.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/view/ventas")
public class DetalleVentaView {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private ProductosRepository productosRepository;


    // =====================================================
    // LISTAR DETALLES DE UNA VENTA
    // =====================================================

    @GetMapping("/{id}/detalles")
    public String detalles(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Venta venta =
                ventaRepository
                        .findById(id)
                        .orElse(null);

        if (venta == null) {

            ra.addFlashAttribute(
                    "success",
                    "Venta no encontrada"
            );

            return "redirect:/view/ventas";
        }

        model.addAttribute(
                "venta",
                venta
        );

        model.addAttribute(
                "detalles",
                detalleVentaRepository
                        .findAll()
                        .stream()
                        .filter(detalle ->
                                detalle.getId_venta() != null
                                        && detalle.getId_venta()
                                        .equals(id)
                        )
                        .toList()
        );

        model.addAttribute(
                "activeMenu",
                "ventas"
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
                "Detalle de Venta"
        );

        model.addAttribute(
                "pageSubtitle",
                "Productos incluidos en la venta seleccionada."
        );

        return "ventas/detalleVenta";
    }


    // =====================================================
    // FORMULARIO NUEVO DETALLE
    // =====================================================

    @GetMapping("/{id}/detalles/form")
    public String form(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Venta venta =
                ventaRepository
                        .findById(id)
                        .orElse(null);

        if (venta == null) {

            ra.addFlashAttribute(
                    "success",
                    "Venta no encontrada"
            );

            return "redirect:/view/ventas";
        }

        DetalleVenta detalle =
                new DetalleVenta();

        detalle.setId_venta(id);
        detalle.setCantidad(1);
        detalle.setPrecio(BigDecimal.ZERO);
        detalle.setSubtotal(BigDecimal.ZERO);

        model.addAttribute(
                "venta",
                venta
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
                "Agregue un producto a la venta seleccionada."
        );

        model.addAttribute(
                "editMode",
                false
        );

        model.addAttribute(
                "activeMenu",
                "ventas"
        );

        return "ventas/detalleVentaForm";
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

        Venta venta =
                ventaRepository
                        .findById(id)
                        .orElse(null);

        if (venta == null) {

            ra.addFlashAttribute(
                    "success",
                    "Venta no encontrada"
            );

            return "redirect:/view/ventas";
        }

        DetalleVenta detalle =
                detalleVentaRepository
                        .findById(detalleId)
                        .orElse(null);

        if (detalle == null) {

            ra.addFlashAttribute(
                    "success",
                    "Detalle de venta no encontrado"
            );

            return "redirect:/view/ventas/"
                    + id
                    + "/detalles";
        }

        model.addAttribute(
                "venta",
                venta
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
                "Actualice la información del producto de la venta."
        );

        model.addAttribute(
                "editMode",
                true
        );

        model.addAttribute(
                "activeMenu",
                "ventas"
        );

        return "ventas/detalleVentaForm";
    }


    // =====================================================
    // GUARDAR DETALLE
    // =====================================================

    @PostMapping("/{id}/detalles/save")
    public String save(
            @PathVariable Long id,
            @ModelAttribute DetalleVenta detalle,
            RedirectAttributes ra) {

        Venta venta =
                ventaRepository
                        .findById(id)
                        .orElse(null);

        if (venta == null) {

            ra.addFlashAttribute(
                    "success",
                    "Venta no encontrada"
            );

            return "redirect:/view/ventas";
        }

        // -------------------------------------------------
        // ASIGNAR VENTA
        // -------------------------------------------------

        detalle.setId_venta(id);

        // -------------------------------------------------
        // VALIDAR CANTIDAD
        // -------------------------------------------------

        if (detalle.getCantidad() == null ||
                detalle.getCantidad() <= 0) {

            detalle.setCantidad(1);
        }

        // -------------------------------------------------
        // VALIDAR PRECIO
        // -------------------------------------------------

        if (detalle.getPrecio() == null) {

            detalle.setPrecio(
                    BigDecimal.ZERO
            );
        }

        // -------------------------------------------------
        // CALCULAR SUBTOTAL
        // -------------------------------------------------

        BigDecimal subtotal =
                detalle.getPrecio()
                        .multiply(
                                BigDecimal.valueOf(
                                        detalle.getCantidad()
                                )
                        );

        detalle.setSubtotal(subtotal);

        boolean nuevo =
                detalle.getId_detalle_venta() == null;

        detalleVentaRepository.save(detalle);

        // -------------------------------------------------
        // RECALCULAR TOTAL
        // -------------------------------------------------

        recalcularTotalVenta(
                venta
        );

        // -------------------------------------------------
        // MENSAJE
        // -------------------------------------------------

        if (nuevo) {

            ra.addFlashAttribute(
                    "success",
                    "Producto agregado a la venta"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Detalle actualizado correctamente"
            );
        }

        return "redirect:/view/ventas/"
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

        if (detalleVentaRepository.existsById(detalleId)) {

            detalleVentaRepository.deleteById(
                    detalleId
            );

            Venta venta =
                    ventaRepository
                            .findById(id)
                            .orElse(null);

            if (venta != null) {

                recalcularTotalVenta(
                        venta
                );
            }

            ra.addFlashAttribute(
                    "success",
                    "Producto eliminado de la venta"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Detalle de venta no encontrado"
            );
        }

        return "redirect:/view/ventas/"
                + id
                + "/detalles";
    }


    // =====================================================
    // RECALCULAR TOTAL DE LA VENTA
    // =====================================================

    private void recalcularTotalVenta(
            Venta venta) {

        BigDecimal subtotal =
                detalleVentaRepository
                        .findAll()
                        .stream()
                        .filter(detalle ->
                                detalle.getId_venta() != null
                                        && detalle.getId_venta()
                                        .equals(
                                                venta.getId_venta()
                                        )
                        )
                        .map(DetalleVenta::getSubtotal)
                        .filter(valor ->
                                valor != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal descuento =
                venta.getDescuento() != null
                        ? venta.getDescuento()
                        : BigDecimal.ZERO;

        BigDecimal impuestos =
                venta.getImpuestos() != null
                        ? venta.getImpuestos()
                        : BigDecimal.ZERO;

        BigDecimal total =
                subtotal
                        .subtract(descuento)
                        .add(impuestos);

        if (total.compareTo(
                BigDecimal.ZERO) < 0) {

            total = BigDecimal.ZERO;
        }

        venta.setTotal(total);

        ventaRepository.save(venta);
    }
}