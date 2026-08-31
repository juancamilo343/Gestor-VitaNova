package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.DetalleVenta;
import com.vitaNova.vitaNova.model.Productos;
import com.vitaNova.vitaNova.model.Venta;
import com.vitaNova.vitaNova.repository.DetalleVentaRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import com.vitaNova.vitaNova.repository.VentaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/detalles-venta")
public class DetalleVentaController {

    private final DetalleVentaRepository detalleVentaRepository;
    private final VentaRepository ventaRepository;
    private final ProductosRepository productosRepository;

    public DetalleVentaController(
            DetalleVentaRepository detalleVentaRepository,
            VentaRepository ventaRepository,
            ProductosRepository productosRepository) {

        this.detalleVentaRepository = detalleVentaRepository;
        this.ventaRepository = ventaRepository;
        this.productosRepository = productosRepository;
    }

    // =====================================================
    // LISTAR TODOS LOS DETALLES
    // =====================================================

    @GetMapping
    public ResponseEntity<List<DetalleVenta>> listar() {

        return ResponseEntity.ok(
                detalleVentaRepository.findAll()
        );
    }

    // =====================================================
    // LISTAR DETALLES DE UNA VENTA
    // =====================================================

    @GetMapping("/venta/{idVenta}")
    public ResponseEntity<?> listarPorVenta(
            @PathVariable Long idVenta) {

        Venta venta =
                ventaRepository
                        .findById(idVenta)
                        .orElse(null);

        if (venta == null) {
            return ResponseEntity.notFound().build();
        }

        List<DetalleVenta> detalles =
                detalleVentaRepository
                        .findAll()
                        .stream()
                        .filter(detalle ->
                                detalle.getId_venta() != null
                                        && detalle.getId_venta()
                                        .equals(idVenta)
                        )
                        .toList();

        return ResponseEntity.ok(detalles);
    }

    // =====================================================
    // BUSCAR DETALLE POR ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<DetalleVenta> buscarPorId(
            @PathVariable Long id) {

        return detalleVentaRepository
                .findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // =====================================================
    // CREAR DETALLE DE VENTA
    // =====================================================

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody DetalleVenta detalle) {

        // -------------------------------------------------
        // VALIDAR VENTA
        // -------------------------------------------------

        if (detalle.getId_venta() == null) {

            return ResponseEntity.badRequest()
                    .body("El ID de la venta es obligatorio.");
        }

        Venta venta =
                ventaRepository
                        .findById(detalle.getId_venta())
                        .orElse(null);

        if (venta == null) {

            return ResponseEntity.badRequest()
                    .body("La venta no existe.");
        }

        // -------------------------------------------------
        // VALIDAR PRODUCTO
        // -------------------------------------------------

        if (detalle.getId_producto() == null) {

            return ResponseEntity.badRequest()
                    .body("El ID del producto es obligatorio.");
        }

        Productos producto =
                productosRepository
                        .findById(detalle.getId_producto())
                        .orElse(null);

        if (producto == null) {

            return ResponseEntity.badRequest()
                    .body("El producto no existe.");
        }

        // -------------------------------------------------
        // VALIDAR CANTIDAD
        // -------------------------------------------------

        if (detalle.getCantidad() == null ||
                detalle.getCantidad() <= 0) {

            return ResponseEntity.badRequest()
                    .body("La cantidad debe ser mayor que cero.");
        }

        // -------------------------------------------------
        // VALIDAR PRECIO
        // -------------------------------------------------

        if (detalle.getPrecio() == null) {

            return ResponseEntity.badRequest()
                    .body("El precio es obligatorio.");
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

        // -------------------------------------------------
        // GUARDAR
        // -------------------------------------------------

        DetalleVenta guardado =
                detalleVentaRepository.save(detalle);

        // -------------------------------------------------
        // RECALCULAR TOTAL
        // -------------------------------------------------

        recalcularTotalVenta(venta);

        return ResponseEntity.ok(guardado);
    }

    // =====================================================
    // ACTUALIZAR DETALLE
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody DetalleVenta datos) {

        DetalleVenta detalle =
                detalleVentaRepository
                        .findById(id)
                        .orElse(null);

        if (detalle == null) {
            return ResponseEntity.notFound().build();
        }

        // -------------------------------------------------
        // VALIDAR CANTIDAD
        // -------------------------------------------------

        if (datos.getCantidad() == null ||
                datos.getCantidad() <= 0) {

            return ResponseEntity.badRequest()
                    .body("La cantidad debe ser mayor que cero.");
        }

        // -------------------------------------------------
        // VALIDAR PRECIO
        // -------------------------------------------------

        if (datos.getPrecio() == null) {

            return ResponseEntity.badRequest()
                    .body("El precio es obligatorio.");
        }

        // -------------------------------------------------
        // ACTUALIZAR DATOS
        // -------------------------------------------------

        detalle.setCantidad(
                datos.getCantidad()
        );

        detalle.setPrecio(
                datos.getPrecio()
        );

        // -------------------------------------------------
        // ACTUALIZAR PRODUCTO
        // -------------------------------------------------

        if (datos.getId_producto() != null) {

            Productos producto =
                    productosRepository
                            .findById(
                                    datos.getId_producto()
                            )
                            .orElse(null);

            if (producto == null) {

                return ResponseEntity.badRequest()
                        .body("El producto no existe.");
            }

            detalle.setId_producto(
                    datos.getId_producto()
            );
        }

        // -------------------------------------------------
        // RECALCULAR SUBTOTAL
        // -------------------------------------------------

        BigDecimal subtotal =
                datos.getPrecio()
                        .multiply(
                                BigDecimal.valueOf(
                                        datos.getCantidad()
                                )
                        );

        detalle.setSubtotal(subtotal);

        // -------------------------------------------------
        // GUARDAR
        // -------------------------------------------------

        DetalleVenta actualizado =
                detalleVentaRepository.save(detalle);

        // -------------------------------------------------
        // RECALCULAR TOTAL DE LA VENTA
        // -------------------------------------------------

        Venta venta =
                ventaRepository
                        .findById(
                                detalle.getId_venta()
                        )
                        .orElse(null);

        if (venta != null) {
            recalcularTotalVenta(venta);
        }

        return ResponseEntity.ok(actualizado);
    }

    // =====================================================
    // ELIMINAR DETALLE
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id) {

        DetalleVenta detalle =
                detalleVentaRepository
                        .findById(id)
                        .orElse(null);

        if (detalle == null) {
            return ResponseEntity.notFound().build();
        }

        Long idVenta =
                detalle.getId_venta();

        detalleVentaRepository.deleteById(id);

        // -------------------------------------------------
        // RECALCULAR TOTAL
        // -------------------------------------------------

        if (idVenta != null) {

            Venta venta =
                    ventaRepository
                            .findById(idVenta)
                            .orElse(null);

            if (venta != null) {
                recalcularTotalVenta(venta);
            }
        }

        return ResponseEntity.ok(
                "Detalle de venta eliminado correctamente."
        );
    }

    // =====================================================
    // RECALCULAR TOTAL DE LA VENTA
    // =====================================================

    private void recalcularTotalVenta(Venta venta) {

        List<DetalleVenta> detalles =
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
                        .toList();

        BigDecimal subtotal =
                detalles.stream()
                        .map(DetalleVenta::getSubtotal)
                        .filter(valor -> valor != null)
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

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        venta.setTotal(total);

        ventaRepository.save(venta);
    }
}