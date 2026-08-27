package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Compra;
import com.vitaNova.vitaNova.model.DetalleCompra;
import com.vitaNova.vitaNova.model.Productos;
import com.vitaNova.vitaNova.repository.CompraRepository;
import com.vitaNova.vitaNova.repository.DetalleCompraRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/detalles-compra")
public class DetalleCompraController {

    private final DetalleCompraRepository detalleCompraRepository;
    private final CompraRepository compraRepository;
    private final ProductosRepository productosRepository;

    public DetalleCompraController(
            DetalleCompraRepository detalleCompraRepository,
            CompraRepository compraRepository,
            ProductosRepository productosRepository) {

        this.detalleCompraRepository = detalleCompraRepository;
        this.compraRepository = compraRepository;
        this.productosRepository = productosRepository;
    }


    // =====================================================
    // LISTAR TODOS LOS DETALLES
    // =====================================================

    @GetMapping
    public ResponseEntity<List<DetalleCompra>> listar() {

        return ResponseEntity.ok(
                detalleCompraRepository.findAll()
        );
    }


    // =====================================================
    // LISTAR DETALLES DE UNA COMPRA
    // =====================================================

    @GetMapping("/compra/{idCompra}")
    public ResponseEntity<?> listarPorCompra(
            @PathVariable Long idCompra) {

        Compra compra = compraRepository
                .findById(idCompra)
                .orElse(null);

        if (compra == null) {
            return ResponseEntity.notFound().build();
        }

        List<DetalleCompra> detalles =
                detalleCompraRepository
                        .findAll()
                        .stream()
                        .filter(detalle ->
                                detalle.getId_compra() != null
                                        && detalle.getId_compra().equals(idCompra)
                        )
                        .toList();

        return ResponseEntity.ok(detalles);
    }


    // =====================================================
    // BUSCAR POR ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<DetalleCompra> buscarPorId(
            @PathVariable Long id) {

        return detalleCompraRepository
                .findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }


    // =====================================================
    // CREAR DETALLE
    // =====================================================

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody DetalleCompra detalle) {

        // -------------------------------------------------
        // VALIDAR COMPRA
        // -------------------------------------------------

        if (detalle.getId_compra() == null) {

            return ResponseEntity.badRequest()
                    .body("El ID de la compra es obligatorio.");
        }

        Compra compra = compraRepository
                .findById(detalle.getId_compra())
                .orElse(null);

        if (compra == null) {

            return ResponseEntity.badRequest()
                    .body("La compra no existe.");
        }


        // -------------------------------------------------
        // VALIDAR PRODUCTO
        // -------------------------------------------------

        if (detalle.getId_producto() == null) {

            return ResponseEntity.badRequest()
                    .body("El ID del producto es obligatorio.");
        }

        Productos producto = productosRepository
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
                    .body("El precio de compra es obligatorio.");
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

        DetalleCompra guardado =
                detalleCompraRepository.save(detalle);


        // -------------------------------------------------
        // RECALCULAR TOTAL DE LA COMPRA
        // -------------------------------------------------

        recalcularTotalCompra(compra);


        return ResponseEntity.ok(guardado);
    }


    // =====================================================
    // ACTUALIZAR DETALLE
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody DetalleCompra datos) {

        DetalleCompra detalle =
                detalleCompraRepository
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
                    .body("El precio de compra es obligatorio.");
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

            Productos producto = productosRepository
                    .findById(datos.getId_producto())
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

        DetalleCompra actualizado =
                detalleCompraRepository.save(detalle);


        // -------------------------------------------------
        // RECALCULAR TOTAL
        // -------------------------------------------------

        Compra compra =
                compraRepository
                        .findById(detalle.getId_compra())
                        .orElse(null);

        if (compra != null) {

            recalcularTotalCompra(compra);
        }


        return ResponseEntity.ok(actualizado);
    }


    // =====================================================
    // ELIMINAR DETALLE
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id) {

        DetalleCompra detalle =
                detalleCompraRepository
                        .findById(id)
                        .orElse(null);

        if (detalle == null) {

            return ResponseEntity.notFound().build();
        }


        Long idCompra =
                detalle.getId_compra();


        // -------------------------------------------------
        // ELIMINAR
        // -------------------------------------------------

        detalleCompraRepository.deleteById(id);


        // -------------------------------------------------
        // RECALCULAR TOTAL
        // -------------------------------------------------

        if (idCompra != null) {

            Compra compra =
                    compraRepository
                            .findById(idCompra)
                            .orElse(null);

            if (compra != null) {

                recalcularTotalCompra(compra);
            }
        }


        return ResponseEntity.ok(
                "Detalle eliminado correctamente."
        );
    }


    // =====================================================
    // RECALCULAR TOTAL DE LA COMPRA
    // =====================================================

    private void recalcularTotalCompra(
            Compra compra) {

        List<DetalleCompra> detalles =
                detalleCompraRepository
                        .findAll()
                        .stream()
                        .filter(detalle ->
                                detalle.getId_compra() != null
                                        && detalle.getId_compra()
                                        .equals(compra.getId_compra())
                        )
                        .toList();


        BigDecimal total = BigDecimal.ZERO;


        for (DetalleCompra detalle : detalles) {

            if (detalle.getSubtotal() != null) {

                total = total.add(
                        detalle.getSubtotal()
                );
            }
        }


        compra.setTotal(total);

        compraRepository.save(compra);
    }
}