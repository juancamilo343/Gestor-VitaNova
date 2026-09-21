package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.DetalleVenta;
import com.vitaNova.vitaNova.model.DetalleVentaLote;
import com.vitaNova.vitaNova.model.Inventario;
import com.vitaNova.vitaNova.model.Lote;
import com.vitaNova.vitaNova.model.Productos;
import com.vitaNova.vitaNova.model.Venta;
import com.vitaNova.vitaNova.repository.DetalleVentaLoteRepository;
import com.vitaNova.vitaNova.repository.DetalleVentaRepository;
import com.vitaNova.vitaNova.repository.InventarioRepository;
import com.vitaNova.vitaNova.repository.LoteRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import com.vitaNova.vitaNova.repository.VentaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/detalles-venta")
public class DetalleVentaController {

    private final DetalleVentaRepository detalleVentaRepository;
    private final DetalleVentaLoteRepository detalleVentaLoteRepository;
    private final VentaRepository ventaRepository;
    private final ProductosRepository productosRepository;
    private final InventarioRepository inventarioRepository;
    private final LoteRepository loteRepository;

    public DetalleVentaController(
            DetalleVentaRepository detalleVentaRepository,
            DetalleVentaLoteRepository detalleVentaLoteRepository,
            VentaRepository ventaRepository,
            ProductosRepository productosRepository,
            InventarioRepository inventarioRepository,
            LoteRepository loteRepository) {

        this.detalleVentaRepository = detalleVentaRepository;
        this.detalleVentaLoteRepository = detalleVentaLoteRepository;
        this.ventaRepository = ventaRepository;
        this.productosRepository = productosRepository;
        this.inventarioRepository = inventarioRepository;
        this.loteRepository = loteRepository;
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
                    .body(
                            "No se pueden agregar productos a una venta anulada o devuelta."
                    );
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

        if (inventario.getStock_actual() == null) {

            return ResponseEntity.badRequest()
                    .body(
                            "El inventario del producto no tiene stock registrado."
                    );
        }


        // -----------------------------------------------------
        // VALIDAR STOCK GENERAL
        // -----------------------------------------------------

        if (inventario.getStock_actual()
                < detalle.getCantidad()) {

            return ResponseEntity.badRequest()
                    .body(
                            "Stock insuficiente para realizar la venta."
                    );
        }


        // -----------------------------------------------------
        // VALIDAR LOTES DISPONIBLES
        // -----------------------------------------------------

        List<Lote> lotesDisponibles =
                obtenerLotesDisponibles(
                        detalle.getId_producto()
                );

        int stockLotes = lotesDisponibles.stream()
                .map(Lote::getCantidad)
                .filter(cantidad -> cantidad != null)
                .mapToInt(Integer::intValue)
                .sum();

        if (stockLotes < detalle.getCantidad()) {

            return ResponseEntity.badRequest()
                    .body(
                            "No hay suficiente cantidad disponible en los lotes para realizar la venta."
                    );
        }


        // -----------------------------------------------------
        // GUARDAR DETALLE
        // -----------------------------------------------------

        DetalleVenta guardado =
                detalleVentaRepository.save(detalle);


        // -----------------------------------------------------
        // DESCONTAR LOTES MEDIANTE FEFO
        // -----------------------------------------------------

        descontarLotesFEFO(
                guardado,
                detalle.getId_producto(),
                detalle.getCantidad()
        );


        // -----------------------------------------------------
        // DESCONTAR INVENTARIO GENERAL
        // -----------------------------------------------------

        inventario.setStock_actual(
                inventario.getStock_actual()
                        - detalle.getCantidad()
        );

        inventarioRepository.save(inventario);


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


        // -----------------------------------------------------
        // BUSCAR VENTA
        // -----------------------------------------------------

        Venta ventaActual =
                ventaRepository.findById(
                        detalle.getId_venta()
                ).orElse(null);

        if (ventaActual != null
                && inventarioYaLiberado(
                ventaActual.getEstado())) {

            return ResponseEntity.badRequest()
                    .body(
                            "No se pueden modificar productos de una venta anulada o devuelta."
                    );
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
                    .body(
                            "El producto no está disponible para la venta."
                    );
        }


        // -----------------------------------------------------
        // OBTENER ASIGNACIONES DE LOTES ANTERIORES
        // -----------------------------------------------------

        List<DetalleVentaLote> asignacionesAnteriores =
                detalleVentaLoteRepository
                        .buscarPorDetalleVenta(id);


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


        // -----------------------------------------------------
        // DEVOLVER LOTES ANTERIORES
        // -----------------------------------------------------

        if (!asignacionesAnteriores.isEmpty()) {

            restaurarLotes(
                    asignacionesAnteriores
            );

            detalleVentaLoteRepository
                    .eliminarPorDetalleVenta(id);

            inventarioAnterior.setStock_actual(
                    inventarioAnterior.getStock_actual()
                            + detalle.getCantidad()
            );

            inventarioRepository.save(
                    inventarioAnterior
            );

        } else {

            // -------------------------------------------------
            // COMPATIBILIDAD CON DETALLES ANTIGUOS
            // QUE NO TENÍAN ASIGNACIÓN DE LOTES
            // -------------------------------------------------

            inventarioAnterior.setStock_actual(
                    inventarioAnterior.getStock_actual()
                            + detalle.getCantidad()
            );

            inventarioRepository.save(
                    inventarioAnterior
            );
        }


        // =====================================================
        // SI CAMBIÓ EL PRODUCTO
        // =====================================================

        if (!idProductoAnterior.equals(idProductoNuevo)) {

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


            if (inventarioNuevo.getStock_actual()
                    < datos.getCantidad()) {

                return ResponseEntity.badRequest()
                        .body(
                                "Stock insuficiente para actualizar la venta."
                        );
            }


            List<Lote> lotesNuevos =
                    obtenerLotesDisponibles(
                            idProductoNuevo
                    );

            int stockLotesNuevos =
                    lotesNuevos.stream()
                            .map(Lote::getCantidad)
                            .filter(cantidad -> cantidad != null)
                            .mapToInt(Integer::intValue)
                            .sum();

            if (stockLotesNuevos < datos.getCantidad()) {

                return ResponseEntity.badRequest()
                        .body(
                                "No hay suficiente cantidad disponible en los lotes del nuevo producto."
                        );
            }


            detalle.setId_producto(
                    idProductoNuevo
            );


            // -------------------------------------------------
            // GUARDAR NUEVOS DATOS
            // -------------------------------------------------

            detalle.setCantidad(
                    datos.getCantidad()
            );

            detalle.setPrecio(
                    datos.getPrecio()
            );


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

            DetalleVenta actualizado =
                    detalleVentaRepository.save(detalle);


            // -------------------------------------------------
            // DESCONTAR NUEVO PRODUCTO POR FEFO
            // -------------------------------------------------

            descontarLotesFEFO(
                    actualizado,
                    idProductoNuevo,
                    datos.getCantidad()
            );


            inventarioNuevo.setStock_actual(
                    inventarioNuevo.getStock_actual()
                            - datos.getCantidad()
            );

            inventarioRepository.save(
                    inventarioNuevo
            );


            Venta venta =
                    ventaRepository.findById(
                            detalle.getId_venta()
                    ).orElse(null);

            if (venta != null) {
                recalcularTotalVenta(venta);
            }


            return ResponseEntity.ok(actualizado);
        }


        // =====================================================
        // MISMO PRODUCTO
        // =====================================================

        // -----------------------------------------------------
        // VALIDAR STOCK PARA LA NUEVA CANTIDAD
        // -----------------------------------------------------

        List<Lote> lotesDisponibles =
                obtenerLotesDisponibles(
                        idProductoNuevo
                );

        int stockLotes =
                lotesDisponibles.stream()
                        .map(Lote::getCantidad)
                        .filter(cantidad -> cantidad != null)
                        .mapToInt(Integer::intValue)
                        .sum();

        if (stockLotes < datos.getCantidad()) {

            return ResponseEntity.badRequest()
                    .body(
                            "No hay suficiente cantidad disponible en los lotes para actualizar la venta."
                    );
        }


        // -----------------------------------------------------
        // ACTUALIZAR DATOS
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
        // GUARDAR DETALLE
        // -----------------------------------------------------

        DetalleVenta actualizado =
                detalleVentaRepository.save(detalle);


        // -----------------------------------------------------
        // DESCONTAR NUEVOS LOTES MEDIANTE FEFO
        // -----------------------------------------------------

        descontarLotesFEFO(
                actualizado,
                idProductoNuevo,
                datos.getCantidad()
        );


        // -----------------------------------------------------
        // DESCONTAR INVENTARIO
        // -----------------------------------------------------

        inventarioAnterior.setStock_actual(
                inventarioAnterior.getStock_actual()
                        - datos.getCantidad()
        );

        inventarioRepository.save(
                inventarioAnterior
        );


        // -----------------------------------------------------
        // RECALCULAR TOTAL
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
                        ? ventaRepository.findById(idVenta)
                          .orElse(null)
                        : null;


        if (ventaActual != null
                && inventarioYaLiberado(
                ventaActual.getEstado())) {

            return ResponseEntity.badRequest()
                    .body(
                            "No se pueden eliminar productos de una venta anulada o devuelta."
                    );
        }


        // -----------------------------------------------------
        // BUSCAR ASIGNACIONES DE LOTES
        // -----------------------------------------------------

        List<DetalleVentaLote> asignaciones =
                detalleVentaLoteRepository
                        .buscarPorDetalleVenta(id);


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
        // DEVOLVER CANTIDAD A LOS LOTES
        // -----------------------------------------------------

        if (!asignaciones.isEmpty()) {

            restaurarLotes(
                    asignaciones
            );

            detalleVentaLoteRepository
                    .eliminarPorDetalleVenta(id);
        }


        // -----------------------------------------------------
        // DEVOLVER CANTIDAD AL INVENTARIO
        // -----------------------------------------------------

        inventario.setStock_actual(
                inventario.getStock_actual()
                        + detalle.getCantidad()
        );

        inventarioRepository.save(
                inventario
        );


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
    // OBTENER LOTES DISPONIBLES PARA FEFO
    // =========================================================

    private List<Lote> obtenerLotesDisponibles(
            Long idProducto) {

        List<Lote> lotes =
                loteRepository.findByIdProductoAndEstado(
                        idProducto,
                        Lote.EstadoLote.ACTIVO
                );

        LocalDate hoy = LocalDate.now();

        return lotes.stream()
                .filter(lote ->
                        lote.getCantidad() != null
                                && lote.getCantidad() > 0
                )
                .filter(lote ->
                        lote.getFecha_vencimiento() != null
                )
                .filter(lote ->
                        !lote.getFecha_vencimiento()
                                .isBefore(hoy)
                )
                .sorted(
                        Comparator.comparing(
                                Lote::getFecha_vencimiento
                        )
                )
                .toList();
    }


    // =========================================================
    // DESCONTAR LOTES MEDIANTE FEFO
    // =========================================================

    private void descontarLotesFEFO(
            DetalleVenta detalle,
            Long idProducto,
            int cantidadSolicitada) {

        List<Lote> lotes =
                obtenerLotesDisponibles(
                        idProducto
                );

        int pendiente =
                cantidadSolicitada;


        for (Lote lote : lotes) {

            if (pendiente <= 0) {
                break;
            }


            int cantidadDisponible =
                    lote.getCantidad();


            int cantidadAConsumir =
                    Math.min(
                            cantidadDisponible,
                            pendiente
                    );


            // -------------------------------------------------
            // DESCONTAR DEL LOTE
            // -------------------------------------------------

            lote.setCantidad(
                    cantidadDisponible
                            - cantidadAConsumir
            );


            // -------------------------------------------------
            // ACTUALIZAR ESTADO
            // -------------------------------------------------

            if (lote.getCantidad() == 0) {

                lote.setEstado(
                        Lote.EstadoLote.AGOTADO
                );
            }


            loteRepository.save(lote);


            // -------------------------------------------------
            // REGISTRAR CONSUMO DEL LOTE
            // -------------------------------------------------

            DetalleVentaLote detalleVentaLote =
                    new DetalleVentaLote();

            detalleVentaLote.setId_detalle_venta(
                    detalle.getId_detalle_venta()
            );

            detalleVentaLote.setId_lote(
                    lote.getId_lote()
            );

            detalleVentaLote.setCantidad(
                    cantidadAConsumir
            );


            detalleVentaLoteRepository.save(
                    detalleVentaLote
            );


            pendiente -= cantidadAConsumir;
        }


        // -----------------------------------------------------
        // VERIFICAR QUE SE HAYA PODIDO COMPLETAR LA VENTA
        // -----------------------------------------------------

        if (pendiente > 0) {

            throw new IllegalStateException(
                    "No hay suficiente cantidad disponible en los lotes para completar la venta."
            );
        }
    }


    // =========================================================
    // RESTAURAR LOTES DE UNA VENTA
    // =========================================================

    private void restaurarLotes(
            List<DetalleVentaLote> asignaciones) {

        for (DetalleVentaLote asignacion : asignaciones) {

            if (asignacion.getId_lote() == null) {
                continue;
            }


            Lote lote =
                    loteRepository.findById(
                            asignacion.getId_lote()
                    ).orElse(null);


            if (lote == null) {
                continue;
            }


            int cantidadActual =
                    lote.getCantidad() != null
                            ? lote.getCantidad()
                            : 0;


            int cantidadDevolver =
                    asignacion.getCantidad() != null
                            ? asignacion.getCantidad()
                            : 0;


            lote.setCantidad(
                    cantidadActual
                            + cantidadDevolver
            );


            // -------------------------------------------------
            // SI SE DEVUELVE STOCK, EL LOTE VUELVE A ACTIVO
            // -------------------------------------------------

            if (lote.getCantidad() > 0
                    && lote.getFecha_vencimiento() != null
                    && !lote.getFecha_vencimiento()
                    .isBefore(LocalDate.now())) {

                lote.setEstado(
                        Lote.EstadoLote.ACTIVO
                );
            }


            loteRepository.save(lote);
        }
    }


    // =========================================================
    // RECALCULAR TOTAL DE LA VENTA
    // =========================================================

    private void recalcularTotalVenta(
            Venta venta) {


        // -----------------------------------------------------
        // OBTENER TODOS LOS DETALLES
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
        // IMPUESTO 19%
        // -----------------------------------------------------

        BigDecimal porcentajeImpuesto =
                new BigDecimal("0.19");


        BigDecimal impuestos =
                subtotal
                        .multiply(
                                porcentajeImpuesto
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        // -----------------------------------------------------
        // CALCULAR TOTAL
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

        venta.setImpuestos(
                impuestos
        );

        venta.setTotal(
                total
        );


        ventaRepository.save(venta);
    }


    // =========================================================
    // VALIDAR PRODUCTO DISPONIBLE
    // =========================================================

    private boolean productoDisponible(
            Productos producto) {

        if (producto.getEstado() == null) {
            return true;
        }

        String estado =
                producto.getEstado().trim();

        return !"INACTIVO".equalsIgnoreCase(estado)
                && !"AGOTADO".equalsIgnoreCase(estado);
    }


    // =========================================================
    // VALIDAR ESTADO DE VENTA
    // =========================================================

    private boolean inventarioYaLiberado(
            Venta.Estado estado) {

        return estado == Venta.Estado.ANULADA
                || estado == Venta.Estado.DEVUELTA;
    }
}
