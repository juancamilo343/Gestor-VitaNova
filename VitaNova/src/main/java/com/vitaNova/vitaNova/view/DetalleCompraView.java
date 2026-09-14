package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Compra;
import com.vitaNova.vitaNova.model.DetalleCompra;
import com.vitaNova.vitaNova.model.Inventario;
import com.vitaNova.vitaNova.repository.CompraRepository;
import com.vitaNova.vitaNova.repository.DetalleCompraRepository;
import com.vitaNova.vitaNova.repository.InventarioRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private InventarioRepository inventarioRepository;


    // =========================================================
    // VER DETALLES
    // =========================================================

    @GetMapping("/{id}/detalles")
    public String detalles(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Compra compra =
                compraRepository
                        .findById(id)
                        .orElse(null);

        if (compra == null) {

            ra.addFlashAttribute(
                    "error",
                    "Compra no encontrada."
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
                                        && detalle.getId_compra()
                                        .equals(id)
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


    // =========================================================
    // FORMULARIO NUEVO DETALLE
    // =========================================================

    @GetMapping("/{id}/detalles/form")
    public String form(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Compra compra =
                compraRepository
                        .findById(id)
                        .orElse(null);

        if (compra == null) {

            ra.addFlashAttribute(
                    "error",
                    "Compra no encontrada."
            );

            return "redirect:/view/compras";
        }


        DetalleCompra detalle =
                new DetalleCompra();

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


    // =========================================================
    // EDITAR DETALLE
    // =========================================================

    @GetMapping("/{id}/detalles/edit/{detalleId}")
    public String edit(
            @PathVariable Long id,
            @PathVariable Long detalleId,
            Model model,
            RedirectAttributes ra) {

        Compra compra =
                compraRepository
                        .findById(id)
                        .orElse(null);

        if (compra == null) {

            ra.addFlashAttribute(
                    "error",
                    "Compra no encontrada."
            );

            return "redirect:/view/compras";
        }


        DetalleCompra detalle =
                detalleCompraRepository
                        .findById(detalleId)
                        .orElse(null);

        if (detalle == null) {

            ra.addFlashAttribute(
                    "error",
                    "Detalle de compra no encontrado."
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


    // =========================================================
    // GUARDAR DETALLE
    // =========================================================

    @PostMapping("/{id}/detalles/save")
    @Transactional
    public String save(
            @PathVariable Long id,
            @ModelAttribute DetalleCompra detalle,
            RedirectAttributes ra) {

        Compra compra =
                compraRepository
                        .findById(id)
                        .orElse(null);

        if (compra == null) {

            ra.addFlashAttribute(
                    "error",
                    "Compra no encontrada."
            );

            return "redirect:/view/compras";
        }


        // =====================================================
        // DETALLE ANTERIOR
        // =====================================================

        DetalleCompra detalleAnterior = null;

        if (detalle.getId_detalle_compra() != null) {

            detalleAnterior =
                    detalleCompraRepository
                            .findById(
                                    detalle.getId_detalle_compra()
                            )
                            .orElse(null);

            if (detalleAnterior == null
                    || !id.equals(
                    detalleAnterior.getId_compra()
            )) {

                ra.addFlashAttribute(
                        "error",
                        "El detalle de compra no es válido."
                );

                return "redirect:/view/compras/"
                        + id
                        + "/detalles";
            }
        }


        // =====================================================
        // VALIDAR PRODUCTO
        // =====================================================

        if (detalle.getId_producto() == null
                || !productosRepository.existsById(
                detalle.getId_producto()
        )) {

            ra.addFlashAttribute(
                    "error",
                    "Debe seleccionar un producto válido."
            );

            return "redirect:/view/compras/"
                    + id
                    + "/detalles/form";
        }


        detalle.setId_compra(id);


        // =====================================================
        // VALIDAR CANTIDAD
        // =====================================================

        if (detalle.getCantidad() == null
                || detalle.getCantidad() <= 0) {

            detalle.setCantidad(1);
        }


        // =====================================================
        // VALIDAR PRECIO
        // =====================================================

        if (detalle.getPrecio() == null) {

            detalle.setPrecio(
                    BigDecimal.ZERO
            );
        }


        // =====================================================
        // CALCULAR SUBTOTAL
        // =====================================================

        BigDecimal subtotal =
                detalle.getPrecio()
                        .multiply(
                                BigDecimal.valueOf(
                                        detalle.getCantidad()
                                )
                        );

        detalle.setSubtotal(subtotal);


        // =====================================================
        // NUEVO DETALLE
        // =====================================================

        boolean nuevo =
                detalle.getId_detalle_compra() == null;


        /*
         * Si la compra ya está recibida:
         *
         * Nuevo detalle  -> aumenta stock.
         * Editar detalle -> ajusta stock.
         *
         * Si está pendiente:
         * no se toca inventario todavía.
         */

        if (esRecibida(compra)) {

            String errorInventario =
                    ajustarInventario(
                            detalleAnterior,
                            detalle
                    );

            if (errorInventario != null) {

                ra.addFlashAttribute(
                        "error",
                        errorInventario
                );

                return "redirect:/view/compras/"
                        + id
                        + "/detalles";
            }
        }


        // =====================================================
        // GUARDAR DETALLE
        // =====================================================

        detalleCompraRepository.save(
                detalle
        );


        // =====================================================
        // RECALCULAR TOTAL
        // =====================================================

        recalcularTotalCompra(compra);


        // =====================================================
        // MENSAJE
        // =====================================================

        if (nuevo) {

            ra.addFlashAttribute(
                    "success",
                    "Producto agregado a la compra. El inventario fue actualizado."
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Detalle actualizado correctamente. El inventario fue actualizado."
            );
        }


        return "redirect:/view/compras/"
                + id
                + "/detalles";
    }


    // =========================================================
    // ELIMINAR DETALLE
    // =========================================================

    @PostMapping("/{id}/detalles/delete/{detalleId}")
    @Transactional
    public String delete(
            @PathVariable Long id,
            @PathVariable Long detalleId,
            RedirectAttributes ra) {

        DetalleCompra detalle =
                detalleCompraRepository
                        .findById(detalleId)
                        .orElse(null);

        if (detalle == null
                || !id.equals(
                detalle.getId_compra()
        )) {

            ra.addFlashAttribute(
                    "error",
                    "Detalle de compra no encontrado."
            );

            return "redirect:/view/compras/"
                    + id
                    + "/detalles";
        }


        Compra compra =
                compraRepository
                        .findById(id)
                        .orElse(null);


        // =====================================================
        // SI LA COMPRA ESTÁ RECIBIDA
        // RETIRAR LO QUE HABÍA INGRESADO
        // =====================================================

        if (compra != null
                && esRecibida(compra)) {

            Inventario inventario =
                    inventarioRepository
                            .buscarPorProductoParaActualizar(
                                    detalle.getId_producto()
                            )
                            .orElse(null);

            if (inventario == null
                    || inventario.getStock_actual() == null) {

                ra.addFlashAttribute(
                        "error",
                        "No es posible eliminar el detalle porque el producto no tiene inventario."
                );

                return "redirect:/view/compras/"
                        + id
                        + "/detalles";
            }


            if (inventario.getStock_actual()
                    < detalle.getCantidad()) {

                ra.addFlashAttribute(
                        "error",
                        "No es posible eliminar el detalle porque ese stock ya fue utilizado."
                );

                return "redirect:/view/compras/"
                        + id
                        + "/detalles";
            }


            inventario.setStock_actual(
                    inventario.getStock_actual()
                            - detalle.getCantidad()
            );

            inventarioRepository.save(
                    inventario
            );
        }


        // =====================================================
        // ELIMINAR DETALLE
        // =====================================================

        detalleCompraRepository.delete(
                detalle
        );


        // =====================================================
        // RECALCULAR TOTAL
        // =====================================================

        if (compra != null) {

            recalcularTotalCompra(
                    compra
            );
        }


        ra.addFlashAttribute(
                "success",
                "Producto eliminado de la compra."
        );


        return "redirect:/view/compras/"
                + id
                + "/detalles";
    }


    // =========================================================
    // VERIFICAR ESTADO RECIBIDA
    // =========================================================

    private boolean esRecibida(
            Compra compra) {

        return compra != null
                && compra.getEstado() != null
                && "RECIBIDA".equalsIgnoreCase(
                compra.getEstado()
        );
    }


    // =========================================================
    // AJUSTAR INVENTARIO
    // =========================================================

    private String ajustarInventario(
            DetalleCompra detalleAnterior,
            DetalleCompra detalleNuevo) {


        // =====================================================
        // VALIDACIONES
        // =====================================================

        if (detalleNuevo.getId_producto() == null) {

            return "Debe seleccionar un producto.";
        }

        if (detalleNuevo.getCantidad() == null
                || detalleNuevo.getCantidad() <= 0) {

            return "La cantidad debe ser mayor que cero.";
        }


        // =====================================================
        // NUEVO DETALLE
        // =====================================================

        if (detalleAnterior == null) {

            Inventario inventarioNuevo =
                    inventarioRepository
                            .buscarPorProductoParaActualizar(
                                    detalleNuevo.getId_producto()
                            )
                            .orElse(null);

            if (inventarioNuevo == null) {

                return "El producto no tiene un registro de inventario.";
            }

            if (inventarioNuevo.getStock_actual() == null) {

                return "El stock actual del producto no está definido.";
            }


            inventarioNuevo.setStock_actual(
                    inventarioNuevo.getStock_actual()
                            + detalleNuevo.getCantidad()
            );

            inventarioRepository.save(
                    inventarioNuevo
            );

            return null;
        }


        // =====================================================
        // MISMO PRODUCTO
        // =====================================================

        if (detalleAnterior.getId_producto()
                .equals(
                        detalleNuevo.getId_producto()
                )) {

            Inventario inventario =
                    inventarioRepository
                            .buscarPorProductoParaActualizar(
                                    detalleNuevo.getId_producto()
                            )
                            .orElse(null);

            if (inventario == null) {

                return "El producto no tiene un registro de inventario.";
            }

            if (inventario.getStock_actual() == null) {

                return "El stock actual del producto no está definido.";
            }


            int diferencia =
                    detalleNuevo.getCantidad()
                            - detalleAnterior.getCantidad();


            // -------------------------------------------------
            // AUMENTÓ LA CANTIDAD COMPRADA
            // -------------------------------------------------

            if (diferencia > 0) {

                inventario.setStock_actual(
                        inventario.getStock_actual()
                                + diferencia
                );
            }


            // -------------------------------------------------
            // DISMINUYÓ LA CANTIDAD COMPRADA
            // -------------------------------------------------

            else if (diferencia < 0) {

                int cantidadARetirar =
                        Math.abs(diferencia);

                if (inventario.getStock_actual()
                        < cantidadARetirar) {

                    return "No es posible disminuir la cantidad porque parte de ese stock ya fue utilizado.";
                }

                inventario.setStock_actual(
                        inventario.getStock_actual()
                                - cantidadARetirar
                );
            }


            inventarioRepository.save(
                    inventario
            );

            return null;
        }


        // =====================================================
        // CAMBIÓ EL PRODUCTO
        // =====================================================

        Inventario inventarioAnterior =
                inventarioRepository
                        .buscarPorProductoParaActualizar(
                                detalleAnterior.getId_producto()
                        )
                        .orElse(null);

        if (inventarioAnterior == null
                || inventarioAnterior.getStock_actual() == null) {

            return "El producto anterior no tiene un registro de inventario.";
        }


        Inventario inventarioNuevo =
                inventarioRepository
                        .buscarPorProductoParaActualizar(
                                detalleNuevo.getId_producto()
                        )
                        .orElse(null);

        if (inventarioNuevo == null
                || inventarioNuevo.getStock_actual() == null) {

            return "El nuevo producto no tiene un registro de inventario.";
        }


        // -----------------------------------------------------
        // VALIDAR QUE SE PUEDA RETIRAR EL PRODUCTO ANTERIOR
        // -----------------------------------------------------

        if (inventarioAnterior.getStock_actual()
                < detalleAnterior.getCantidad()) {

            return "No es posible cambiar el producto porque el stock anterior ya fue utilizado.";
        }


        // -----------------------------------------------------
        // DEVOLVER PRODUCTO ANTERIOR
        // -----------------------------------------------------

        inventarioAnterior.setStock_actual(
                inventarioAnterior.getStock_actual()
                        - detalleAnterior.getCantidad()
        );


        // -----------------------------------------------------
        // AGREGAR NUEVO PRODUCTO
        // -----------------------------------------------------

        inventarioNuevo.setStock_actual(
                inventarioNuevo.getStock_actual()
                        + detalleNuevo.getCantidad()
        );


        // -----------------------------------------------------
        // GUARDAR INVENTARIOS
        // -----------------------------------------------------

        inventarioRepository.save(
                inventarioAnterior
        );

        inventarioRepository.save(
                inventarioNuevo
        );

        return null;
    }


    // =========================================================
    // RECALCULAR TOTAL
    // =========================================================

    private void recalcularTotalCompra(
            Compra compra) {

        BigDecimal total =
                detalleCompraRepository
                        .findAll()
                        .stream()
                        .filter(detalle ->
                                detalle.getId_compra() != null
                                        && detalle.getId_compra()
                                        .equals(
                                                compra.getId_compra()
                                        )
                        )
                        .map(
                                DetalleCompra::getSubtotal
                        )
                        .filter(
                                subtotal -> subtotal != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        compra.setTotal(total);

        compraRepository.save(
                compra
        );
    }
}

