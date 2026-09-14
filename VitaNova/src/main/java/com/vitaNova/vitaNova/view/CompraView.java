package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Compra;
import com.vitaNova.vitaNova.model.DetalleCompra;
import com.vitaNova.vitaNova.model.Inventario;
import com.vitaNova.vitaNova.repository.CompraRepository;
import com.vitaNova.vitaNova.repository.DetalleCompraRepository;
import com.vitaNova.vitaNova.repository.InventarioRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import com.vitaNova.vitaNova.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/view/compras")
public class CompraView {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @Autowired
    private InventarioRepository inventarioRepository;


    // =========================================================
    // LISTA
    // =========================================================

    @GetMapping
    public String lista(Model model) {

        model.addAttribute(
                "compras",
                compraRepository.findAll()
        );

        model.addAttribute("activeMenu", "compras");
        model.addAttribute("userName", "Administrador");
        model.addAttribute("userRole", "Farmacia Central");
        model.addAttribute("pageTitle", "Gestión de Compras");
        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de las compras realizadas."
        );

        return "compras/compras";
    }


    // =========================================================
    // NUEVA COMPRA
    // =========================================================

    @GetMapping("/form")
    public String form(Model model) {

        Compra compra = new Compra();

        compra.setFecha(LocalDate.now());
        compra.setTotal(BigDecimal.ZERO);

        /*
         * La compra se registra como recibida.
         * Al guardar sus productos, estos ingresarán al inventario.
         */
        compra.setEstado("RECIBIDA");

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


    // =========================================================
    // EDITAR COMPRA
    // =========================================================

    @GetMapping("/edit/{id}")
    public String edit(
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


    // =========================================================
    // GUARDAR COMPRA + DETALLES + INVENTARIO
    // =========================================================

    @PostMapping("/save")
    @Transactional
    public String save(
            @ModelAttribute Compra compra,

            @RequestParam(
                    value = "id_productos[]",
                    required = false
            )
            List<Long> idProductos,

            @RequestParam(
                    value = "cantidades[]",
                    required = false
            )
            List<Integer> cantidades,

            @RequestParam(
                    value = "precios[]",
                    required = false
            )
            List<BigDecimal> precios,

            RedirectAttributes ra) {

        boolean nuevo =
                compra.getId_compra() == null;


        // =====================================================
        // VALORES POR DEFECTO
        // =====================================================

        if (compra.getFecha() == null) {

            compra.setFecha(
                    LocalDate.now()
            );
        }

        if (compra.getEstado() == null
                || compra.getEstado().isBlank()) {

            compra.setEstado("RECIBIDA");
        }

        if (compra.getTotal() == null) {

            compra.setTotal(
                    BigDecimal.ZERO
            );
        }


        // =====================================================
        // CREAR NUEVA COMPRA
        // =====================================================

        if (nuevo) {

            /*
             * Primero guardamos la compra para obtener
             * su id_compra.
             */

            compra.setTotal(
                    BigDecimal.ZERO
            );

            Compra compraGuardada =
                    compraRepository.save(
                            compra
                    );


            // -------------------------------------------------
            // VALIDAR PRODUCTOS
            // -------------------------------------------------

            if (idProductos == null
                    || cantidades == null
                    || precios == null
                    || idProductos.isEmpty()) {

                ra.addFlashAttribute(
                        "error",
                        "Debe agregar al menos un producto a la compra."
                );

                return "redirect:/view/compras/edit/"
                        + compraGuardada.getId_compra();
            }


            if (idProductos.size()
                    != cantidades.size()
                    || idProductos.size()
                    != precios.size()) {

                ra.addFlashAttribute(
                        "error",
                        "Los datos de los productos de la compra no son válidos."
                );

                return "redirect:/view/compras/edit/"
                        + compraGuardada.getId_compra();
            }


            // -------------------------------------------------
            // CREAR DETALLES
            // -------------------------------------------------

            List<DetalleCompra> detalles =
                    new ArrayList<>();

            BigDecimal total =
                    BigDecimal.ZERO;


            for (int i = 0;
                 i < idProductos.size();
                 i++) {

                Long idProducto =
                        idProductos.get(i);

                Integer cantidad =
                        cantidades.get(i);

                BigDecimal precio =
                        precios.get(i);


                if (idProducto == null
                        || !productosRepository.existsById(idProducto)) {

                    ra.addFlashAttribute(
                            "error",
                            "Uno de los productos seleccionados no existe."
                    );

                    return "redirect:/view/compras/edit/"
                            + compraGuardada.getId_compra();
                }


                if (cantidad == null
                        || cantidad <= 0) {

                    ra.addFlashAttribute(
                            "error",
                            "La cantidad debe ser mayor que cero."
                    );

                    return "redirect:/view/compras/edit/"
                            + compraGuardada.getId_compra();
                }


                if (precio == null
                        || precio.compareTo(BigDecimal.ZERO) < 0) {

                    ra.addFlashAttribute(
                            "error",
                            "El precio de compra no puede ser negativo."
                    );

                    return "redirect:/view/compras/edit/"
                            + compraGuardada.getId_compra();
                }


                BigDecimal subtotal =
                        precio.multiply(
                                BigDecimal.valueOf(
                                        cantidad
                                )
                        );


                DetalleCompra detalle =
                        new DetalleCompra();

                detalle.setId_compra(
                        compraGuardada.getId_compra()
                );

                detalle.setId_producto(
                        idProducto
                );

                detalle.setCantidad(
                        cantidad
                );

                detalle.setPrecio(
                        precio
                );

                detalle.setSubtotal(
                        subtotal
                );


                detalles.add(
                        detalle
                );


                total =
                        total.add(
                                subtotal
                        );
            }


            // -------------------------------------------------
            // GUARDAR DETALLES
            // -------------------------------------------------

            detalleCompraRepository.saveAll(
                    detalles
            );


            // -------------------------------------------------
            // ACTUALIZAR TOTAL
            // -------------------------------------------------

            compraGuardada.setTotal(
                    total
            );

            compraRepository.save(
                    compraGuardada
            );


            // -------------------------------------------------
            // ACTUALIZAR INVENTARIO
            // -------------------------------------------------

            if (esRecibida(compraGuardada)) {

                String errorInventario =
                        aumentarInventario(
                                detalles
                        );

                if (errorInventario != null) {

                    ra.addFlashAttribute(
                            "error",
                            errorInventario
                    );

                    return "redirect:/view/compras/edit/"
                            + compraGuardada.getId_compra();
                }
            }


            ra.addFlashAttribute(
                    "success",
                    "Compra registrada correctamente. El inventario fue actualizado."
            );

            return "redirect:/view/compras/"
                    + compraGuardada.getId_compra()
                    + "/detalles";
        }


        // =====================================================
        // ACTUALIZAR COMPRA EXISTENTE
        // =====================================================

        Compra compraAnterior =
                compraRepository
                        .findById(
                                compra.getId_compra()
                        )
                        .orElse(null);

        if (compraAnterior == null) {

            ra.addFlashAttribute(
                    "error",
                    "Compra no encontrada."
            );

            return "redirect:/view/compras";
        }


        boolean eraRecibida =
                esRecibida(
                        compraAnterior
                );

        boolean seraRecibida =
                esRecibida(
                        compra
                );


        // =====================================================
        // PENDIENTE -> RECIBIDA
        // =====================================================

        if (!eraRecibida
                && seraRecibida) {

            String errorInventario =
                    aumentarInventario(
                            detallesDeCompra(
                                    compra.getId_compra()
                            )
                    );

            if (errorInventario != null) {

                ra.addFlashAttribute(
                        "error",
                        errorInventario
                );

                return "redirect:/view/compras/edit/"
                        + compra.getId_compra();
            }
        }


        // =====================================================
        // RECIBIDA -> PENDIENTE/CANCELADA
        // =====================================================

        if (eraRecibida
                && !seraRecibida) {

            String errorInventario =
                    revertirInventario(
                            detallesDeCompra(
                                    compra.getId_compra()
                            )
                    );

            if (errorInventario != null) {

                ra.addFlashAttribute(
                        "error",
                        errorInventario
                );

                return "redirect:/view/compras/edit/"
                        + compra.getId_compra();
            }
        }


        // =====================================================
        // GUARDAR CAMBIOS
        // =====================================================

        compraRepository.save(
                compra
        );


        ra.addFlashAttribute(
                "success",
                "Compra actualizada correctamente."
        );

        return "redirect:/view/compras";
    }


    // =========================================================
    // ELIMINAR COMPRA
    // =========================================================

    @PostMapping("/delete/{id}")
    @Transactional
    public String delete(
            @PathVariable Long id,
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


        List<DetalleCompra> detalles =
                detallesDeCompra(id);


        if (esRecibida(compra)) {

            String errorInventario =
                    revertirInventario(
                            detalles
                    );

            if (errorInventario != null) {

                ra.addFlashAttribute(
                        "error",
                        errorInventario
                );

                return "redirect:/view/compras";
            }
        }


        detalleCompraRepository.deleteAll(
                detalles
        );

        compraRepository.delete(
                compra
        );


        ra.addFlashAttribute(
                "success",
                "Compra eliminada correctamente."
        );

        return "redirect:/view/compras";
    }


    // =========================================================
    // VERIFICAR RECIBIDA
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
    // OBTENER DETALLES
    // =========================================================

    private List<DetalleCompra> detallesDeCompra(
            Long idCompra) {

        return detalleCompraRepository
                .findAll()
                .stream()
                .filter(detalle ->
                        detalle.getId_compra() != null
                                && detalle.getId_compra()
                                .equals(idCompra)
                )
                .toList();
    }


    // =========================================================
    // AUMENTAR INVENTARIO
    // =========================================================

    private String aumentarInventario(
            List<DetalleCompra> detalles) {

        if (detalles == null
                || detalles.isEmpty()) {

            return "La compra no tiene productos registrados.";
        }


        // -----------------------------------------------------
        // VALIDAR DATOS
        // -----------------------------------------------------

        for (DetalleCompra detalle : detalles) {

            if (detalle.getId_producto() == null) {

                return "Un detalle de compra no tiene producto.";
            }

            if (detalle.getCantidad() == null
                    || detalle.getCantidad() <= 0) {

                return "La cantidad de un producto no es válida.";
            }
        }


        // -----------------------------------------------------
        // SUMAR O CREAR INVENTARIO
        // -----------------------------------------------------

        for (DetalleCompra detalle : detalles) {

            Inventario inventario =
                    inventarioRepository
                            .buscarPorProductoParaActualizar(
                                    detalle.getId_producto()
                            )
                            .orElse(null);


            // -------------------------------------------------
            // SI NO EXISTE, CREAR INVENTARIO
            // -------------------------------------------------

            if (inventario == null) {

                inventario = new Inventario();

                inventario.setId_producto(
                        detalle.getId_producto()
                );

                inventario.setStock_actual(
                        detalle.getCantidad()
                );

                /*
                 * El stock mínimo y máximo son valores
                 * de configuración inicial.
                 *
                 * Posteriormente, pueden modificarse
                 * desde el módulo Inventario.
                 */
                inventario.setStock_minimo(0);

                inventario.setStock_maximo(0);
            }


            // -------------------------------------------------
            // SI EXISTE, SUMAR STOCK
            // -------------------------------------------------

            else {

                if (inventario.getStock_actual() == null) {

                    inventario.setStock_actual(0);
                }

                inventario.setStock_actual(
                        inventario.getStock_actual()
                                + detalle.getCantidad()
                );
            }


            inventarioRepository.save(
                    inventario
            );
        }


        return null;
    }


    // =========================================================
    // REVERTIR INVENTARIO
    // =========================================================

    private String revertirInventario(
            List<DetalleCompra> detalles) {

        if (detalles == null
                || detalles.isEmpty()) {

            return null;
        }


        // -----------------------------------------------------
        // VALIDAR
        // -----------------------------------------------------

        for (DetalleCompra detalle : detalles) {

            Inventario inventario =
                    inventarioRepository
                            .buscarPorProductoParaActualizar(
                                    detalle.getId_producto()
                            )
                            .orElse(null);


            if (inventario == null
                    || inventario.getStock_actual() == null) {

                return "No se puede revertir la compra porque el producto no tiene inventario.";
            }


            if (inventario.getStock_actual()
                    < detalle.getCantidad()) {

                return "No se puede revertir la compra porque parte del stock ya fue utilizado.";
            }
        }


        // -----------------------------------------------------
        // RESTAR
        // -----------------------------------------------------

        for (DetalleCompra detalle : detalles) {

            Inventario inventario =
                    inventarioRepository
                            .buscarPorProductoParaActualizar(
                                    detalle.getId_producto()
                            )
                            .orElseThrow();


            inventario.setStock_actual(
                    inventario.getStock_actual()
                            - detalle.getCantidad()
            );


            inventarioRepository.save(
                    inventario
            );
        }


        return null;
    }
}