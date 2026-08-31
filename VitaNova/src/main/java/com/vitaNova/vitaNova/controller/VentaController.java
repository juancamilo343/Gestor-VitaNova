package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Venta;
import com.vitaNova.vitaNova.repository.VentaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaRepository ventaRepository;

    public VentaController(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    // =====================================================
    // LISTAR TODAS LAS VENTAS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<Venta>> listar() {

        return ResponseEntity.ok(
                ventaRepository.findAll()
        );
    }

    // =====================================================
    // BUSCAR VENTA POR ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<Venta> buscarPorId(
            @PathVariable Long id) {

        return ventaRepository
                .findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // =====================================================
    // CREAR VENTA
    // =====================================================

    @PostMapping
    public ResponseEntity<Venta> crear(
            @RequestBody Venta venta) {

        Venta nuevaVenta =
                ventaRepository.save(venta);

        return ResponseEntity.ok(nuevaVenta);
    }

    // =====================================================
    // ACTUALIZAR VENTA
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<Venta> actualizar(
            @PathVariable Long id,
            @RequestBody Venta datos) {

        Venta venta =
                ventaRepository
                        .findById(id)
                        .orElse(null);

        if (venta == null) {
            return ResponseEntity.notFound().build();
        }

        if (datos.getFecha() != null) {
            venta.setFecha(datos.getFecha());
        }

        if (datos.getTotal() != null) {
            venta.setTotal(datos.getTotal());
        }

        if (datos.getDescuento() != null) {
            venta.setDescuento(datos.getDescuento());
        }

        if (datos.getImpuestos() != null) {
            venta.setImpuestos(datos.getImpuestos());
        }

        if (datos.getEstado() != null) {
            venta.setEstado(datos.getEstado());
        }

        if (datos.getId_cliente() != null) {
            venta.setId_cliente(datos.getId_cliente());
        }

        if (datos.getId_empleado() != null) {
            venta.setId_empleado(datos.getId_empleado());
        }

        if (datos.getId_metodo_pago() != null) {
            venta.setId_metodo_pago(
                    datos.getId_metodo_pago()
            );
        }

        Venta actualizada =
                ventaRepository.save(venta);

        return ResponseEntity.ok(actualizada);
    }

    // =====================================================
    // ELIMINAR VENTA
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id) {

        if (!ventaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        ventaRepository.deleteById(id);

        return ResponseEntity.ok(
                "Venta eliminada correctamente."
        );
    }
}