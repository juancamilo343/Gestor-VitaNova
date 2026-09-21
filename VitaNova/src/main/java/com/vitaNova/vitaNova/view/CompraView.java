package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Compra;
import com.vitaNova.vitaNova.model.DetalleCompra;
import com.vitaNova.vitaNova.model.Inventario;
import com.vitaNova.vitaNova.model.Lote;
import com.vitaNova.vitaNova.repository.CompraRepository;
import com.vitaNova.vitaNova.repository.DetalleCompraRepository;
import com.vitaNova.vitaNova.repository.InventarioRepository;
import com.vitaNova.vitaNova.repository.LoteRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import com.vitaNova.vitaNova.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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

    @Autowired
    private LoteRepository loteRepository;


    // =========================================================
    // LISTA
    // =========================================================

    @GetMapping
    public String lista(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) String search) {

        if (search != null && !search.trim().isEmpty()) {

            model.addAttribute(
                    "compras",
                    compraRepository.buscarCompras(search.trim())
            );

        } else {

            model.addAttribute(
                    "compras",
                    compraRepository.findAll()
            );
        }

        model.addAttribute(
                "search",
                search
        );

        model.addAttribute("activeMenu", "compras");
        model.addAttribute("userName", "Administrador");
        model.addAttribute("userRole", "Farmacia Central");
        model.addAttribute("pageTitle", "Gestión de Compras");

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de las compras realizadas."
        );

        model.addAttribute(
                "puedeModificar",
                puedeModificar(authentication)
        );

        return "compras/compras";
    }


    // =========================================================
    // NUEVA COMPRA
    // =========================================================

    @GetMapping("/form")
    public String form(
            Model model,
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para registrar compras."
            );

            return "redirect:/view/compras";
        }

        Compra compra = new Compra();

        compra.setFecha(LocalDate.now());
        compra.setTotal(BigDecimal.ZERO);
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
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para editar compras."
            );

            return "redirect:/view/compras";
        }

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
    // GUARDAR COMPRA + DETALLES + LOTES + INVENTARIO
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

            @RequestParam(
                    value = "lote_numeros[]",
                    required = false
            )
            List<String> loteNumeros,

            @RequestParam(
                    value = "fechas_fabricacion[]",
                    required = false
            )
            List<LocalDate> fechasFabricacion,

            @RequestParam(
                    value = "fechas_vencimiento[]",
                    required = false
            )
            List<LocalDate> fechasVencimiento,

            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para registrar o modificar compras."
            );

            return "redirect:/view/compras";
        }

        boolean nuevo =
                compra.getId_compra() == null;


        // =====================================================
        // VALORES POR DEFECTO
        // =====================================================

        if (nuevo) {

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
                    || loteNumeros == null
                    || fechasFabricacion == null
                    || fechasVencimiento == null
                    || idProductos.isEmpty()) {

                ra.addFlashAttribute(
                        "error",
                        "Debe agregar al menos un producto a la compra."
                );

                return "redirect:/view/compras/edit/"
                        + compraGuardada.getId_compra();
            }


            // -------------------------------------------------
            // VALIDAR CANTIDAD DE DATOS
            // -------------------------------------------------

            if (idProductos.size()
                    != cantidades.size()
                    || idProductos.size()
                    != precios.size()
                    || idProductos.size()
                    != loteNumeros.size()
                    || idProductos.size()
                    != fechasFabricacion.size()
                    || idProductos.size()
                    != fechasVencimiento.size()) {

                ra.addFlashAttribute(
                        "error",
                        "Los datos de los productos y lotes de la compra no son válidos."
                );

                return "redirect:/view/compras/edit/"
                        + compraGuardada.getId_compra();
            }


            // -------------------------------------------------
            // CREAR DETALLES Y LOTES
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

                String numeroLote =
                        loteNumeros.get(i);

                LocalDate fechaFabricacion =
                        fechasFabricacion.get(i);

                LocalDate fechaVencimiento =
                        fechasVencimiento.get(i);


                // -------------------------------------------------
                // VALIDAR PRODUCTO
                // -------------------------------------------------

                if (idProducto == null
                        || !productosRepository.existsById(idProducto)) {

                    ra.addFlashAttribute(
                            "error",
                            "Uno de los productos seleccionados no existe."
                    );

                    return "redirect:/view/compras/edit/"
                            + compraGuardada.getId_compra();
                }


                // -------------------------------------------------
                // VALIDAR CANTIDAD
                // -------------------------------------------------

                if (cantidad == null
                        || cantidad <= 0) {

                    ra.addFlashAttribute(
                            "error",
                            "La cantidad debe ser mayor que cero."
                    );

                    return "redirect:/view/compras/edit/"
                            + compraGuardada.getId_compra();
                }


                // -------------------------------------------------
                // VALIDAR PRECIO
                // -------------------------------------------------

                if (precio == null
                        || precio.compareTo(BigDecimal.ZERO) < 0) {

                    ra.addFlashAttribute(
                            "error",
                            "El precio de compra no puede ser negativo."
                    );

                    return "redirect:/view/compras/edit/"
                            + compraGuardada.getId_compra();
                }


                // -------------------------------------------------
                // VALIDAR NÚMERO DE LOTE
                // -------------------------------------------------

                if (numeroLote == null
                        || numeroLote.trim().isEmpty()) {

                    ra.addFlashAttribute(
                            "error",
                            "El número de lote es obligatorio."
                    );

                    return "redirect:/view/compras/edit/"
                            + compraGuardada.getId_compra();
                }

                numeroLote =
                        numeroLote.trim();


                if (numeroLote.length() > 50) {

                    ra.addFlashAttribute(
                            "error",
                            "El número de lote no puede superar los 50 caracteres."
                    );

                    return "redirect:/view/compras/edit/"
                            + compraGuardada.getId_compra();
                }


                // -------------------------------------------------
                // VALIDAR FECHA DE FABRICACIÓN
                // -------------------------------------------------

                if (fechaFabricacion == null) {

                    ra.addFlashAttribute(
                            "error",
                            "La fecha de fabricación es obligatoria."
                    );

                    return "redirect:/view/compras/edit/"
                            + compraGuardada.getId_compra();
                }


                // -------------------------------------------------
                // VALIDAR FECHA DE VENCIMIENTO
                // -------------------------------------------------

                if (fechaVencimiento == null) {

                    ra.addFlashAttribute(
                            "error",
                            "La fecha de vencimiento es obligatoria."
                    );

                    return "redirect:/view/compras/edit/"
                            + compraGuardada.getId_compra();
                }


                // -------------------------------------------------
                // VALIDAR ORDEN DE FECHAS
                // -------------------------------------------------

                if (fechaVencimiento.isBefore(
                        fechaFabricacion)) {

                    ra.addFlashAttribute(
                            "error",
                            "La fecha de vencimiento no puede ser anterior a la fecha de fabricación."
                    );

                    return "redirect:/view/compras/edit/"
                            + compraGuardada.getId_compra();
                }


                // -------------------------------------------------
                // VALIDAR VENCIMIENTO
                // -------------------------------------------------

                if (fechaVencimiento.isBefore(
                        LocalDate.now())) {

                    ra.addFlashAttribute(
                            "error",
                            "No se puede registrar una compra con un lote ya vencido."
                    );

                    return "redirect:/view/compras/edit/"
                            + compraGuardada.getId_compra();
                }


                // -------------------------------------------------
                // CALCULAR SUBTOTAL
                // -------------------------------------------------

                BigDecimal subtotal =
                        precio.multiply(
                                BigDecimal.valueOf(
                                        cantidad
                                )
                        );


                // -------------------------------------------------
                // CREAR LOTE
                // -------------------------------------------------

                Lote lote =
                        new Lote();

                lote.setNumero_lote(
                        numeroLote
                );

                lote.setFecha_fabricacion(
                        fechaFabricacion
                );

                lote.setFecha_vencimiento(
                        fechaVencimiento
                );

                lote.setCantidad(
                        cantidad
                );

                lote.setEstado(
                        Lote.EstadoLote.ACTIVO
                );

                lote.setId_producto(
                        idProducto
                );


                Lote loteGuardado =
                        loteRepository.save(
                                lote
                        );


                // -------------------------------------------------
                // CREAR DETALLE
                // -------------------------------------------------

                DetalleCompra detalle =
                        new DetalleCompra();

                detalle.setId_compra(
                        compraGuardada.getId_compra()
                );

                detalle.setId_producto(
                        idProducto
                );

                detalle.setId_lote(
                        loteGuardado.getId_lote()
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
                    "Compra registrada correctamente. El lote y el inventario fueron actualizados."
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

            List<DetalleCompra> detalles =
                    detallesDeCompra(
                            compra.getId_compra()
                    );

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
                        + compra.getId_compra();
            }


            String errorLotes =
                    activarLotesDeCompra(
                            detalles
                    );

            if (errorLotes != null) {

                ra.addFlashAttribute(
                        "error",
                        errorLotes
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

            List<DetalleCompra> detalles =
                    detallesDeCompra(
                            compra.getId_compra()
                    );

            String errorInventario =
                    revertirInventario(
                            detalles
                    );

            if (errorInventario != null) {

                ra.addFlashAttribute(
                        "error",
                        errorInventario
                );

                return "redirect:/view/compras/edit/"
                        + compra.getId_compra();
            }


            String errorLotes =
                    revertirLotesDeCompra(
                            detalles
                    );

            if (errorLotes != null) {

                ra.addFlashAttribute(
                        "error",
                        errorLotes
                );

                return "redirect:/view/compras/edit/"
                        + compra.getId_compra();
            }
        }


        // =====================================================
        // CONSERVAR FECHA ORIGINAL
        // =====================================================

        compra.setFecha(
                compraAnterior.getFecha()
        );


        // =====================================================
        // CONSERVAR TOTAL ORIGINAL
        // =====================================================

        compra.setTotal(
                compraAnterior.getTotal()
        );


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
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para eliminar compras."
            );

            return "redirect:/view/compras";
        }

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


            String errorLotes =
                    revertirLotesDeCompra(
                            detalles
                    );

            if (errorLotes != null) {

                ra.addFlashAttribute(
                        "error",
                        errorLotes
                );

                return "redirect:/view/compras";
            }
        }


        detalleCompraRepository.deleteAll(
                detalles
        );

        eliminarLotesDeCompra(
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
    // PERMISOS
    // =========================================================

    private boolean puedeModificar(
            Authentication authentication) {

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMINISTRADOR")
                                || authority.getAuthority()
                                .equals("ROLE_FARMACEUTICO")
                );
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

        for (DetalleCompra detalle : detalles) {

            if (detalle.getId_producto() == null) {
                return "Un detalle de compra no tiene producto.";
            }

            if (detalle.getCantidad() == null
                    || detalle.getCantidad() <= 0) {
                return "La cantidad de un producto no es válida.";
            }

            if (detalle.getId_lote() == null) {
                return "Un detalle de compra no tiene lote asociado.";
            }
        }

        for (DetalleCompra detalle : detalles) {

            Inventario inventario =
                    inventarioRepository
                            .buscarPorProductoParaActualizar(
                                    detalle.getId_producto()
                            )
                            .orElse(null);

            if (inventario == null) {

                inventario =
                        new Inventario();

                inventario.setId_producto(
                        detalle.getId_producto()
                );

                inventario.setStock_actual(
                        detalle.getCantidad()
                );

                inventario.setStock_minimo(0);

                inventario.setStock_maximo(0);

            } else {

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

        for (DetalleCompra detalle : detalles) {

            if (detalle.getId_producto() == null) {
                return "Un detalle de compra no tiene producto.";
            }

            if (detalle.getCantidad() == null
                    || detalle.getCantidad() <= 0) {
                return "La cantidad de un producto no es válida.";
            }

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


    // =========================================================
    // ACTIVAR LOTES DE UNA COMPRA
    // =========================================================

    private String activarLotesDeCompra(
            List<DetalleCompra> detalles) {

        if (detalles == null
                || detalles.isEmpty()) {

            return null;
        }

        for (DetalleCompra detalle : detalles) {

            if (detalle.getId_lote() == null) {
                continue;
            }

            Lote lote =
                    loteRepository
                            .findById(
                                    detalle.getId_lote()
                            )
                            .orElse(null);

            if (lote == null) {
                return "No se encontró el lote asociado a uno de los productos.";
            }

            if (lote.getEstado()
                    == Lote.EstadoLote.RETIRADO) {

                return "No se puede recibir la compra porque uno de sus lotes está retirado.";
            }

            if (lote.getFecha_vencimiento() != null
                    && lote.getFecha_vencimiento()
                    .isBefore(LocalDate.now())) {

                return "No se puede recibir la compra porque uno de sus lotes está vencido.";
            }

            lote.setEstado(
                    lote.getCantidad() != null
                            && lote.getCantidad() > 0
                            ? Lote.EstadoLote.ACTIVO
                            : Lote.EstadoLote.AGOTADO
            );

            loteRepository.save(
                    lote
            );
        }

        return null;
    }


    // =========================================================
    // REVERTIR LOTES DE UNA COMPRA
    // =========================================================

    private String revertirLotesDeCompra(
            List<DetalleCompra> detalles) {

        if (detalles == null
                || detalles.isEmpty()) {

            return null;
        }

        for (DetalleCompra detalle : detalles) {

            if (detalle.getId_lote() == null) {
                continue;
            }

            Lote lote =
                    loteRepository
                            .findById(
                                    detalle.getId_lote()
                            )
                            .orElse(null);

            if (lote == null) {
                return "No se encontró el lote asociado a uno de los productos.";
            }

            lote.setEstado(
                    Lote.EstadoLote.AGOTADO
            );

            loteRepository.save(
                    lote
            );
        }

        return null;
    }


    // =========================================================
    // ELIMINAR LOTES DE UNA COMPRA
    // =========================================================

    private void eliminarLotesDeCompra(
            List<DetalleCompra> detalles) {

        if (detalles == null
                || detalles.isEmpty()) {

            return;
        }

        for (DetalleCompra detalle : detalles) {

            if (detalle.getId_lote() == null) {
                continue;
            }

            if (loteRepository.existsById(
                    detalle.getId_lote())) {

                loteRepository.deleteById(
                        detalle.getId_lote()
                );
            }
        }
    }
}