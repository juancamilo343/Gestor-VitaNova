package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.DetalleVenta;
import com.vitaNova.vitaNova.model.Inventario;
import com.vitaNova.vitaNova.model.Productos;
import com.vitaNova.vitaNova.model.Venta;
import com.vitaNova.vitaNova.repository.DetalleVentaRepository;
import com.vitaNova.vitaNova.repository.InventarioRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import com.vitaNova.vitaNova.repository.VentaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("/api/detalles-venta")
public class DetalleVentaController {

    private final DetalleVentaRepository detalleVentaRepository;
    private final VentaRepository ventaRepository;
    private final ProductosRepository productosRepository;
    private final InventarioRepository inventarioRepository;

    public DetalleVentaController(
            DetalleVentaRepository detalleVentaRepository,
            VentaRepository ventaRepository,
            ProductosRepository productosRepository,
            InventarioRepository inventarioRepository) {

        this.detalleVentaRepository = detalleVentaRepository;
        this.ventaRepository = ventaRepository;
        this.productosRepository = productosRepository;
        this.inventarioRepository = inventarioRepository;
    }


    // =========================================================
    // LISTAR TODOS LOS DETALLES
    // =========================================================

    @GetMapping
    public ResponseEntity<List<DetalleVenta>> listar() {

        return ResponseEntity.ok(
                detalleVentaRepository.findAll()
        );
    }


    // =========================================================
    // LISTAR DETALLES DE UNA VENTA
    // =========================================================

    @GetMapping("/venta/{idVenta}")
    public ResponseEntity<?> listarPorVenta(
            @PathVariable Long idVenta) {

        Venta venta =
                ventaRepository.findById(idVenta).orElse(null);

        if (venta == null) {
            return ResponseEntity.notFound().build();
        }

        List<DetalleVenta> detalles =
                detalleVentaRepository.buscarPorVenta(idVenta);

        return ResponseEntity.ok(detalles);
    }


    // =========================================================
    // BUSCAR DETALLE POR ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<DetalleVenta> buscarPorId(
            @PathVariable Long id) {

        return detalleVentaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }


    // =========================================================
    // CREAR DETALLE DE VENTA
    // =========================================================

    @PostMapping
    @Transactional
    public ResponseEntity<?> crear(
            @RequestBody DetalleVenta detalle) {


        // -----------------------------------------------------
        // VALIDAR VENTA
        // -----------------------------------------------------

        if (detalle.getId_venta() == null) {

            return ResponseEntity.badRequest()
                    .body("El ID de la venta es obligatorio.");
        }


        Venta venta =
                ventaRepository.findById(
                        detalle.getId_venta()
                ).orElse(null);

        if (venta == null) {

            return ResponseEntity.badRequest()
                    .body("La venta no existe.");
        }


        // -----------------------------------------------------
        // VALIDAR PRODUCTO
        // -----------------------------------------------------

        if (detalle.getId_producto() == null) {

            return ResponseEntity.badRequest()
                    .body("El ID del producto es obligatorio.");
        }


        Productos producto =
                productosRepository.findById(
                        detalle.getId_producto()
                ).orElse(null);

        if (producto == null) {

            return ResponseEntity.badRequest()
                    .body("El producto no existe.");
        }

        if (!productoDisponible(producto)) {

            return ResponseEntity.badRequest()
                    .body("El producto no está disponible para la venta.");
        }

        if (inventarioYaLiberado(venta.getEstado())) {

            return ResponseEntity.badRequest()
                    .body("No se pueden agregar productos a una venta anulada o devuelta.");
        }


        // -----------------------------------------------------
        // VALIDAR CANTIDAD
        // -----------------------------------------------------

        if (detalle.getCantidad() == null
                || detalle.getCantidad() <= 0) {

            return ResponseEntity.badRequest()
                    .body("La cantidad debe ser mayor que cero.");
        }


        // -----------------------------------------------------
        // VALIDAR PRECIO
        // -----------------------------------------------------

        if (detalle.getPrecio() == null
                || detalle.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {

            return ResponseEntity.badRequest()
                    .body("El precio debe ser mayor que cero.");
        }


        // -----------------------------------------------------
        // CALCULAR SUBTOTAL
        // -----------------------------------------------------

        BigDecimal subtotal =
                detalle.getPrecio()
                        .multiply(
                                BigDecimal.valueOf(
                                        detalle.getCantidad()
                                )
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        detalle.setSubtotal(subtotal);


        // -----------------------------------------------------
        // BUSCAR INVENTARIO
        // -----------------------------------------------------

        Inventario inventario =
                inventarioRepository
                        .buscarPorProductoParaActualizar(
                                detalle.getId_producto()
                        )
                        .orElse(null);

        if (inventario == null) {

            return ResponseEntity.badRequest()
                    .body(
                            "El producto no tiene un registro de inventario."
                    );
        }


        // -----------------------------------------------------
        // VALIDAR STOCK
        // -----------------------------------------------------

        if (inventario.getStock_actual() == null
                || inventario.getStock_actual()
                < detalle.getCantidad()) {

            return ResponseEntity.badRequest()
                    .body(
                            "Stock insuficiente para realizar la venta."
                    );
        }


        // -----------------------------------------------------
        // DESCONTAR INVENTARIO
        // -----------------------------------------------------

        inventario.setStock_actual(
                inventario.getStock_actual()
                        - detalle.getCantidad()
        );

        inventarioRepository.save(inventario);


        // -----------------------------------------------------
        // GUARDAR DETALLE
        // -----------------------------------------------------

        DetalleVenta guardado =
                detalleVentaRepository.save(detalle);


        // -----------------------------------------------------
        // RECALCULAR VENTA
        // -----------------------------------------------------

        recalcularTotalVenta(venta);


        return ResponseEntity.ok(guardado);
    }


    // =========================================================
    // ACTUALIZAR DETALLE
    // =========================================================

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody DetalleVenta datos) {


        // -----------------------------------------------------
        // BUSCAR DETALLE
        // -----------------------------------------------------

        DetalleVenta detalle =
                detalleVentaRepository.findById(id)
                        .orElse(null);

        if (detalle == null) {

            return ResponseEntity.notFound().build();
        }


        // -----------------------------------------------------
        // VALIDAR CANTIDAD
        // -----------------------------------------------------

        if (datos.getCantidad() == null
                || datos.getCantidad() <= 0) {

            return ResponseEntity.badRequest()
                    .body(
                            "La cantidad debe ser mayor que cero."
                    );
        }


        // -----------------------------------------------------
        // VALIDAR PRECIO
        // -----------------------------------------------------

        if (datos.getPrecio() == null
                || datos.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {

            return ResponseEntity.badRequest()
                    .body("El precio debe ser mayor que cero.");
        }

        Venta ventaActual =
                ventaRepository.findById(detalle.getId_venta()).orElse(null);

        if (ventaActual != null && inventarioYaLiberado(ventaActual.getEstado())) {

            return ResponseEntity.badRequest()
                    .body("No se pueden modificar productos de una venta anulada o devuelta.");
        }


        // -----------------------------------------------------
        // PRODUCTO ANTERIOR / NUEVO
        // -----------------------------------------------------

        Long idProductoAnterior =
                detalle.getId_producto();

        Long idProductoNuevo =
                datos.getId_producto() != null
                        ? datos.getId_producto()
                        : idProductoAnterior;


        if (idProductoNuevo == null) {

            return ResponseEntity.badRequest()
                    .body(
                            "El ID del producto es obligatorio."
                    );
        }


        // -----------------------------------------------------
        // VALIDAR PRODUCTO NUEVO
        // -----------------------------------------------------

        Productos producto =
                productosRepository.findById(
                        idProductoNuevo
                ).orElse(null);

        if (producto == null) {

            return ResponseEntity.badRequest()
                    .body("El producto no existe.");
        }

        if (!productoDisponible(producto)) {

            return ResponseEntity.badRequest()
                    .body("El producto no está disponible para la venta.");
        }


        // -----------------------------------------------------
        // INVENTARIO DEL PRODUCTO ANTERIOR
        // -----------------------------------------------------

        Inventario inventarioAnterior =
                inventarioRepository
                        .buscarPorProductoParaActualizar(
                                idProductoAnterior
                        )
                        .orElse(null);

        if (inventarioAnterior == null
                || inventarioAnterior.getStock_actual() == null) {

            return ResponseEntity.badRequest()
                    .body(
                            "El producto original no tiene inventario disponible."
                    );
        }


        // =====================================================
        // MISMO PRODUCTO
        // =====================================================

        if (idProductoAnterior.equals(idProductoNuevo)) {

            int diferencia =
                    datos.getCantidad()
                            - detalle.getCantidad();


            // ---------------------------------------------
            // SE ESTÁ AUMENTANDO LA CANTIDAD
            // ---------------------------------------------

            if (diferencia > 0
                    && inventarioAnterior.getStock_actual()
                    < diferencia) {

                return ResponseEntity.badRequest()
                        .body(
                                "Stock insuficiente para actualizar la venta."
                        );
            }


            // ---------------------------------------------
            // AJUSTAR INVENTARIO
            // ---------------------------------------------

            inventarioAnterior.setStock_actual(
                    inventarioAnterior.getStock_actual()
                            - diferencia
            );

            inventarioRepository.save(
                    inventarioAnterior
            );
        }


        // =====================================================
        // CAMBIÓ EL PRODUCTO
        // =====================================================

        else {

            Inventario inventarioNuevo =
                    inventarioRepository
                            .buscarPorProductoParaActualizar(
                                    idProductoNuevo
                            )
                            .orElse(null);

            if (inventarioNuevo == null
                    || inventarioNuevo.getStock_actual() == null) {

                return ResponseEntity.badRequest()
                        .body(
                                "El nuevo producto no tiene inventario disponible."
                        );
            }


            // ---------------------------------------------
            // VALIDAR STOCK DEL NUEVO PRODUCTO
            // ---------------------------------------------

            if (inventarioNuevo.getStock_actual()
                    < datos.getCantidad()) {

                return ResponseEntity.badRequest()
                        .body(
                                "Stock insuficiente para actualizar la venta."
                        );
            }


            // ---------------------------------------------
            // DEVOLVER PRODUCTO ANTERIOR
            // ---------------------------------------------

            inventarioAnterior.setStock_actual(
                    inventarioAnterior.getStock_actual()
                            + detalle.getCantidad()
            );


            // ---------------------------------------------
            // DESCONTAR NUEVO PRODUCTO
            // ---------------------------------------------

            inventarioNuevo.setStock_actual(
                    inventarioNuevo.getStock_actual()
                            - datos.getCantidad()
            );


            inventarioRepository.save(
                    inventarioAnterior
            );

            inventarioRepository.save(
                    inventarioNuevo
            );
        }


        // -----------------------------------------------------
        // ACTUALIZAR DATOS DEL DETALLE
        // -----------------------------------------------------

        detalle.setCantidad(
                datos.getCantidad()
        );

        detalle.setPrecio(
                datos.getPrecio()
        );

        detalle.setId_producto(
                idProductoNuevo
        );


        // -----------------------------------------------------
        // RECALCULAR SUBTOTAL
        // -----------------------------------------------------

        BigDecimal subtotal =
                datos.getPrecio()
                        .multiply(
                                BigDecimal.valueOf(
                                        datos.getCantidad()
                                )
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        detalle.setSubtotal(subtotal);


        // -----------------------------------------------------
        // GUARDAR CAMBIOS
        // -----------------------------------------------------

        DetalleVenta actualizado =
                detalleVentaRepository.save(detalle);


        // -----------------------------------------------------
        // RECALCULAR TOTAL DE LA VENTA
        // -----------------------------------------------------

        Venta venta =
                ventaRepository.findById(
                        detalle.getId_venta()
                ).orElse(null);

        if (venta != null) {

            recalcularTotalVenta(venta);
        }


        return ResponseEntity.ok(actualizado);
    }


    // =========================================================
    // ELIMINAR DETALLE
    // =========================================================

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> eliminar(
            @PathVariable Long id) {


        // -----------------------------------------------------
        // BUSCAR DETALLE
        // -----------------------------------------------------

        DetalleVenta detalle =
                detalleVentaRepository.findById(id)
                        .orElse(null);

        if (detalle == null) {

            return ResponseEntity.notFound().build();
        }


        Long idVenta =
                detalle.getId_venta();

        Venta ventaActual =
                idVenta != null
                        ? ventaRepository.findById(idVenta).orElse(null)
                        : null;

        if (ventaActual != null && inventarioYaLiberado(ventaActual.getEstado())) {

            return ResponseEntity.badRequest()
                    .body("No se pueden eliminar productos de una venta anulada o devuelta.");
        }


        // -----------------------------------------------------
        // BUSCAR INVENTARIO
        // -----------------------------------------------------

        Inventario inventario =
                inventarioRepository
                        .buscarPorProductoParaActualizar(
                                detalle.getId_producto()
                        )
                        .orElse(null);

        if (inventario == null
                || inventario.getStock_actual() == null) {

            return ResponseEntity.badRequest()
                    .body(
                            "El producto del detalle no tiene inventario disponible."
                    );
        }


        // -----------------------------------------------------
        // DEVOLVER CANTIDAD AL INVENTARIO
        // -----------------------------------------------------

        inventario.setStock_actual(
                inventario.getStock_actual()
                        + detalle.getCantidad()
        );

        inventarioRepository.save(inventario);


        // -----------------------------------------------------
        // ELIMINAR DETALLE
        // -----------------------------------------------------

        detalleVentaRepository.deleteById(id);


        // -----------------------------------------------------
        // RECALCULAR TOTAL
        // -----------------------------------------------------

        if (idVenta != null) {

            Venta venta =
                    ventaRepository.findById(idVenta)
                            .orElse(null);

            if (venta != null) {

                recalcularTotalVenta(venta);
            }
        }


        return ResponseEntity.ok(
                "Detalle de venta eliminado correctamente."
        );
    }


    // =========================================================
    // RECALCULAR TOTAL DE LA VENTA
    // =========================================================

    private void recalcularTotalVenta(Venta venta) {


        // -----------------------------------------------------
        // OBTENER TODOS LOS DETALLES DE LA VENTA
        // -----------------------------------------------------

        List<DetalleVenta> detalles =
                detalleVentaRepository.buscarPorVenta(
                        venta.getId_venta()
                );


        // -----------------------------------------------------
        // CALCULAR SUBTOTAL
        // -----------------------------------------------------

        BigDecimal subtotal =
                detalles.stream()
                        .map(
                                DetalleVenta::getSubtotal
                        )
                        .filter(
                                valor -> valor != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // -----------------------------------------------------
        // OBTENER DESCUENTO
        // -----------------------------------------------------

        BigDecimal descuento =
                venta.getDescuento() != null
                        ? venta.getDescuento()
                        : BigDecimal.ZERO;


        // -----------------------------------------------------
        // IMPUESTO AUTOMÁTICO
        // -----------------------------------------------------

        BigDecimal porcentajeImpuesto =
                new BigDecimal("0.19");


        BigDecimal impuestos =
                subtotal
                        .multiply(porcentajeImpuesto)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        // -----------------------------------------------------
        // CALCULAR TOTAL
        //
        // SUBTOTAL - DESCUENTO + IMPUESTOS
        // -----------------------------------------------------

        BigDecimal total =
                subtotal
                        .subtract(descuento)
                        .add(impuestos);


        // -----------------------------------------------------
        // EVITAR TOTAL NEGATIVO
        // -----------------------------------------------------

        if (total.compareTo(BigDecimal.ZERO) < 0) {

            total = BigDecimal.ZERO;
        }


        // -----------------------------------------------------
        // GUARDAR IMPUESTOS Y TOTAL
        // -----------------------------------------------------

        venta.setImpuestos(impuestos);

        venta.setTotal(total);


        ventaRepository.save(venta);
    }

    private boolean productoDisponible(Productos producto) {

        if (producto.getEstado() == null) {
            return true;
        }

        String estado = producto.getEstado().trim();

        return !"INACTIVO".equalsIgnoreCase(estado)
                && !"AGOTADO".equalsIgnoreCase(estado);
    }

    private boolean inventarioYaLiberado(Venta.Estado estado) {
        return estado == Venta.Estado.ANULADA
                || estado == Venta.Estado.DEVUELTA;
    }
}