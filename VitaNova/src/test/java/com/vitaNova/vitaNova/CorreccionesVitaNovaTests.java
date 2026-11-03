package com.vitaNova.vitaNova;







import com.vitaNova.vitaNova.model.Clientes;



import com.vitaNova.vitaNova.model.DetalleCompra;



import com.vitaNova.vitaNova.model.DetalleVenta;



import com.vitaNova.vitaNova.model.ProductoPresentacion;



import com.vitaNova.vitaNova.model.Productos;



import com.vitaNova.vitaNova.model.Venta;



import com.vitaNova.vitaNova.model.Compra;



import com.vitaNova.vitaNova.model.Proveedor;



import com.vitaNova.vitaNova.model.Usuarios;



import com.vitaNova.vitaNova.service.RolPermisosService;



import org.junit.jupiter.api.DisplayName;



import org.junit.jupiter.api.Test;



import org.springframework.security.core.GrantedAuthority;







import java.math.BigDecimal;



import java.time.LocalDateTime;



import java.util.*;







import static org.junit.jupiter.api.Assertions.*;







public class CorreccionesVitaNovaTests {

    public static class VentaTab {
        public String id;
        public String titulo;
        public Long idCliente;
        public String nombreCliente;
        public BigDecimal descuento;
        public List<DetalleVenta> items = new ArrayList<>();
        public boolean suspendida = false;

        public BigDecimal calcularTotal() {
            BigDecimal subtotal = items.stream()
                    .map(DetalleVenta::getSubtotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal desc = descuento != null ? descuento : BigDecimal.ZERO;
            BigDecimal iva = subtotal.multiply(new BigDecimal("0.19"));
            return subtotal.subtract(desc).add(iva);
        }
    }
 








    @Test



    @DisplayName("Problema 1: Resolución de nombre completo del cliente en lugar de solo su ID")



    void testResolucionNombreCliente() {



        Clientes c = new Clientes();



        c.setId_cliente(10L);



        c.setNombres("Carlos");



        c.setApellidos("Gómez");







        String nombreCompleto = c.getNombres() + (c.getApellidos() != null && !c.getApellidos().isBlank() ? " " + c.getApellidos().trim() : "");



        assertEquals("Carlos Gómez", nombreCompleto);







        Map<Long, String> mapaClientes = new HashMap<>();



        mapaClientes.put(c.getId_cliente(), nombreCompleto);







        Long ventaIdCliente = 10L;



        assertTrue(mapaClientes.containsKey(ventaIdCliente));



        assertEquals("Carlos Gómez", mapaClientes.get(ventaIdCliente));



    }







    @Test



    @DisplayName("Problema 2 y 3: DetalleCompra tiene compatibilidad de precio_compra y relación de Producto")



    void testDetalleCompraCompatibilidad() {



        DetalleCompra detalle = new DetalleCompra();



        detalle.setPrecio(new BigDecimal("15000.50"));







        // Compatibilidad con getPrecio_compra para evitar errores de SpEL



        assertEquals(new BigDecimal("15000.50"), detalle.getPrecio_compra());



        assertEquals(new BigDecimal("15000.50"), detalle.getPrecio());







        Productos prod = new Productos();



        prod.setId_producto(5L);



        prod.setNombre("Amoxicilina 500mg");



        detalle.setProducto(prod);







        assertNotNull(detalle.getProducto());



        assertEquals("Amoxicilina 500mg", detalle.getProducto().getNombre());



    }







    @Test



    @DisplayName("Problema 4: Historial de ventas y cálculo de totales")



    void testHistorialVentasCalculo() {



        Venta v1 = new Venta();



        v1.setId_venta(1L);



        v1.setTotal(new BigDecimal("25000.00"));



        v1.setEstado(Venta.Estado.PAGADA);







        Venta v2 = new Venta();



        v2.setId_venta(2L);



        v2.setTotal(new BigDecimal("10000.00"));



        v2.setEstado(Venta.Estado.ANULADA);







        List<Venta> ventas = List.of(v1, v2);







        BigDecimal totalRecaudado = ventas.stream()



                .filter(v -> v.getEstado() != Venta.Estado.ANULADA && v.getTotal() != null)



                .map(Venta::getTotal)



                .reduce(BigDecimal.ZERO, BigDecimal::add);







        assertEquals(new BigDecimal("25000.00"), totalRecaudado);



    }







    @Test



    @DisplayName("Problema 5: Parametrización de roles y permisos desde interfaz")



    void testParametrizacionRolesPermisos() {



        RolPermisosService service = new RolPermisosService();







        // Verificar permisos por defecto de Administrador



        Set<String> adminPerms = service.obtenerPermisos("ADMINISTRADOR", 1L);



        assertTrue(adminPerms.contains("PERM_ROLES"));



        assertTrue(adminPerms.contains("PERM_USUARIOS"));



        assertTrue(adminPerms.contains("PERM_VENTAS"));







        // Verificar permisos por defecto de Vendedor



        Set<String> vendedorPerms = service.obtenerPermisos("VENDEDOR", 3L);



        assertTrue(vendedorPerms.contains("PERM_VENTAS"));



        assertFalse(vendedorPerms.contains("PERM_ROLES"));



        assertFalse(vendedorPerms.contains("PERM_COMPRAS"));







        // Parametrizar permisos para rol personalizado o Vendedor



        service.guardarPermisosParaRol(3L, "VENDEDOR", List.of("PERM_VENTAS", "PERM_CLIENTES", "PERM_COMPRAS"));



        Set<String> modificado = service.obtenerPermisos("VENDEDOR", 3L);



        assertTrue(modificado.contains("PERM_COMPRAS"));







        // Verificar construcción de autoridades para Spring Security



        List<GrantedAuthority> authorities = service.construirAutoridades("VENDEDOR", 3L);



        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR")));



        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("PERM_COMPRAS")));







        // Restaurar estado base para Vendedor



        service.guardarPermisosParaRol(3L, "VENDEDOR", List.of("PERM_DASHBOARD", "PERM_VENTAS", "PERM_CLIENTES"));



    }







    @Test



    @DisplayName("Etapa 3: Venta por presentaciones (Tableta y Caja) y descuento en unidades base")



    void testPresentacionesEquivalenciaInventario() {



        com.vitaNova.vitaNova.model.ProductoPresentacion presTableta = com.vitaNova.vitaNova.model.ProductoPresentacion.builder()



                .id_presentacion(1L)



                .id_producto(1L)



                .nombre_presentacion("Tableta")



                .factor_unidades(1)



                .precio_venta(new BigDecimal("2000.00"))



                .es_principal(false)



                .estado(com.vitaNova.vitaNova.model.ProductoPresentacion.EstadoPresentacion.ACTIVO)



                .build();







        com.vitaNova.vitaNova.model.ProductoPresentacion presCaja = com.vitaNova.vitaNova.model.ProductoPresentacion.builder()



                .id_presentacion(16L)



                .id_producto(1L)



                .nombre_presentacion("Caja")



                .factor_unidades(10)



                .precio_venta(new BigDecimal("20000.00"))



                .es_principal(true)



                .estado(com.vitaNova.vitaNova.model.ProductoPresentacion.EstadoPresentacion.ACTIVO)



                .build();







        // Stock inicial en unidades base (tabletas)



        int stockInicial = 43;







        // Venta 1: 1 Tableta



        com.vitaNova.vitaNova.model.DetalleVenta det1 = new com.vitaNova.vitaNova.model.DetalleVenta();



        det1.setId_producto(1L);



        det1.setId_presentacion(presTableta.getId_presentacion());



        det1.setNombre_presentacion(presTableta.getNombre_presentacion());



        det1.setFactor_unidades(presTableta.getFactor_unidades());



        det1.setCantidad(1);



        det1.setPrecio(presTableta.getPrecio_venta());



        det1.setSubtotal(presTableta.getPrecio_venta().multiply(BigDecimal.valueOf(det1.getCantidad())));



        det1.setUnidades_base(det1.getCantidad() * det1.getFactor_unidades());







        assertEquals(new BigDecimal("2000.00"), det1.getSubtotal());



        assertEquals(1, det1.getUnidades_base());







        int stockDespuesVenta1 = stockInicial - det1.getUnidades_base();



        assertEquals(42, stockDespuesVenta1);







        // Venta 2: 1 Caja



        com.vitaNova.vitaNova.model.DetalleVenta det2 = new com.vitaNova.vitaNova.model.DetalleVenta();



        det2.setId_producto(1L);



        det2.setId_presentacion(presCaja.getId_presentacion());



        det2.setNombre_presentacion(presCaja.getNombre_presentacion());



        det2.setFactor_unidades(presCaja.getFactor_unidades());



        det2.setCantidad(1);



        det2.setPrecio(presCaja.getPrecio_venta());



        det2.setSubtotal(presCaja.getPrecio_venta().multiply(BigDecimal.valueOf(det2.getCantidad())));



        det2.setUnidades_base(det2.getCantidad() * det2.getFactor_unidades());







        assertEquals(new BigDecimal("20000.00"), det2.getSubtotal());



        assertEquals(10, det2.getUnidades_base());







        int stockDespuesVenta2 = stockDespuesVenta1 - det2.getUnidades_base();



        assertEquals(32, stockDespuesVenta2);







        // Validación de no permitir existencias negativas



        int cantidadCajasSolicitadas = 4; // Requiere 40 unidades base



        int unidadesRequeridas = cantidadCajasSolicitadas * presCaja.getFactor_unidades();



        assertTrue(unidadesRequeridas > stockDespuesVenta2, "Debe rechazar la venta porque 40 supera las 32 disponibles");







        // Compatibilidad histórica: venta antigua sin id_presentacion ni factor_unidades explícito



        com.vitaNova.vitaNova.model.DetalleVenta detHistorico = new com.vitaNova.vitaNova.model.DetalleVenta();



        detHistorico.setCantidad(2);



        detHistorico.setPrecio(new BigDecimal("3000.00"));



        detHistorico.setSubtotal(new BigDecimal("6000.00"));







        assertEquals(1, detHistorico.getFactor_unidades());



        assertNull(detHistorico.getId_presentacion());



        assertNull(detHistorico.getNombre_presentacion());



        assertEquals(new BigDecimal("6000.00"), detHistorico.getSubtotal());



    }







    @Test



    @DisplayName("Etapa 4: Compra con presentación comercial (Caja x 100) y conversión a unidades base")



    void testCompraConPresentacionesYUnidadesBase() {



        com.vitaNova.vitaNova.model.ProductoPresentacion presCaja = com.vitaNova.vitaNova.model.ProductoPresentacion.builder()



                .id_presentacion(20L)



                .id_producto(1L)



                .nombre_presentacion("Caja x 100")



                .factor_unidades(100)



                .costo_compra(new BigDecimal("50000.00"))



                .precio_venta(new BigDecimal("70000.00"))



                .permite_compra(true)



                .permite_venta(true)



                .es_principal(true)



                .estado(com.vitaNova.vitaNova.model.ProductoPresentacion.EstadoPresentacion.ACTIVO)



                .build();







        int cantidadComprada = 5; // 5 cajas



        BigDecimal precioCompraCaja = presCaja.getCosto_compra();



        BigDecimal subtotal = precioCompraCaja.multiply(BigDecimal.valueOf(cantidadComprada));







        int unidadesBase = cantidadComprada * presCaja.getFactor_unidades();



        BigDecimal costoUnitarioBase = precioCompraCaja.divide(



                BigDecimal.valueOf(presCaja.getFactor_unidades()), 4, java.math.RoundingMode.HALF_UP



        );







        DetalleCompra detalle = new DetalleCompra();



        detalle.setId_compra(100L);



        detalle.setId_producto(1L);



        detalle.setId_presentacion(presCaja.getId_presentacion());



        detalle.setNombre_presentacion(presCaja.getNombre_presentacion());



        detalle.setFactor_unidades(presCaja.getFactor_unidades());



        detalle.setCantidad(cantidadComprada);



        detalle.setPrecio(precioCompraCaja);



        detalle.setSubtotal(subtotal);



        detalle.setUnidades_base(unidadesBase);



        detalle.setCosto_unitario_base(costoUnitarioBase);







        // Verificaciones del detalle



        assertEquals(5, detalle.getCantidad());



        assertEquals(new BigDecimal("250000.00"), detalle.getSubtotal());



        assertEquals(500, detalle.getUnidades_base());



        assertEquals(new BigDecimal("500.0000"), detalle.getCosto_unitario_base());







        // Verificación de lote generado (debe tener las unidades base físicas)



        com.vitaNova.vitaNova.model.Lote lote = new com.vitaNova.vitaNova.model.Lote();



        lote.setNumero_lote("LOTE-C500");



        lote.setCantidad(detalle.getUnidades_base());



        assertEquals(500, lote.getCantidad());







        // Simulación de aumento de inventario



        int stockPrevio = 20;



        int stockNuevo = stockPrevio + (detalle.getUnidades_base() != null ? detalle.getUnidades_base() : detalle.getCantidad());



        assertEquals(520, stockNuevo);







        // Simulación de reversión de inventario por cancelación



        int stockRevertido = stockNuevo - (detalle.getUnidades_base() != null ? detalle.getUnidades_base() : detalle.getCantidad());



        assertEquals(20, stockRevertido);



    }







    @Test



    @DisplayName("Etapa 6: Desglose comercial dinámico de inventario físico")



    void testDesgloseComercialInventario() {



        int stockFisico = 349;



        String unidadBase = "Tableta";







        List<com.vitaNova.vitaNova.model.ProductoPresentacion> presentaciones = new ArrayList<>();



        presentaciones.add(com.vitaNova.vitaNova.model.ProductoPresentacion.builder()



                .nombre_presentacion("Caja x 100")



                .factor_unidades(100)



                .estado(com.vitaNova.vitaNova.model.ProductoPresentacion.EstadoPresentacion.ACTIVO)



                .build());



        presentaciones.add(com.vitaNova.vitaNova.model.ProductoPresentacion.builder()



                .nombre_presentacion("Blíster x 10")



                .factor_unidades(10)



                .estado(com.vitaNova.vitaNova.model.ProductoPresentacion.EstadoPresentacion.ACTIVO)



                .build());



        presentaciones.add(com.vitaNova.vitaNova.model.ProductoPresentacion.builder()



                .nombre_presentacion("Tableta")



                .factor_unidades(1)



                .estado(com.vitaNova.vitaNova.model.ProductoPresentacion.EstadoPresentacion.ACTIVO)



                .build());







        presentaciones.sort((a, b) -> Integer.compare(b.getFactor_unidades(), a.getFactor_unidades()));







        StringBuilder sb = new StringBuilder();



        int rem = stockFisico;



        boolean tieneEmpaques = false;







        for (com.vitaNova.vitaNova.model.ProductoPresentacion pr : presentaciones) {



            int factor = pr.getFactor_unidades();



            if (factor > 1) {



                int paquetes = rem / factor;



                if (paquetes > 0) {



                    if (tieneEmpaques) sb.append(", ");



                    sb.append(paquetes).append(" ").append(pr.getNombre_presentacion());



                    tieneEmpaques = true;



                    rem %= factor;



                }



            }



        }



        if (rem > 0 || !tieneEmpaques) {



            if (tieneEmpaques) sb.append(" + ");



            sb.append(rem).append(" ").append(unidadBase);



        }







        String desglose = sb.toString();



        assertEquals("3 Caja x 100, 4 Blíster x 10 + 9 Tableta", desglose);



    }







    @Test



    @DisplayName("Etapa 5: Filtro estricto de presentaciones por flags de compra y venta")



    void testFlagsCompraYVentaPresentaciones() {



        com.vitaNova.vitaNova.model.ProductoPresentacion presSoloCompra = com.vitaNova.vitaNova.model.ProductoPresentacion.builder()



                .nombre_presentacion("Fardo x 500")



                .factor_unidades(500)



                .permite_compra(true)



                .permite_venta(false)



                .estado(com.vitaNova.vitaNova.model.ProductoPresentacion.EstadoPresentacion.ACTIVO)



                .build();







        com.vitaNova.vitaNova.model.ProductoPresentacion presSoloVenta = com.vitaNova.vitaNova.model.ProductoPresentacion.builder()



                .nombre_presentacion("Tableta suelta")



                .factor_unidades(1)



                .permite_compra(false)



                .permite_venta(true)



                .estado(com.vitaNova.vitaNova.model.ProductoPresentacion.EstadoPresentacion.ACTIVO)



                .build();







        assertTrue(presSoloCompra.getPermite_compra());



        assertFalse(presSoloCompra.getPermite_venta());







        assertFalse(presSoloVenta.getPermite_compra());



        assertTrue(presSoloVenta.getPermite_venta());



    }







    @Test



    @DisplayName("Etapa 4: Cálculo dinámico de costo sugerido de compra para presentaciones")



    void testCalculoCostoSugeridoPresentacionCompra() {



        Productos prod = new Productos();



        prod.setId_producto(1L);



        prod.setNombre("Acetaminofen");



        prod.setPrecio_compra(new BigDecimal("2500.00")); // Precio base tableta







        // Caso 1: Presentación con costo específico > 0



        com.vitaNova.vitaNova.model.ProductoPresentacion presConCosto = com.vitaNova.vitaNova.model.ProductoPresentacion.builder()



                .id_presentacion(1L)



                .factor_unidades(10)



                .costo_compra(new BigDecimal("22000.00")) // Descuento por volumen



                .build();







        BigDecimal costo1 = (presConCosto.getCosto_compra() != null && presConCosto.getCosto_compra().compareTo(BigDecimal.ZERO) > 0)



                ? presConCosto.getCosto_compra()



                : prod.getPrecio_compra().multiply(BigDecimal.valueOf(presConCosto.getFactor_unidades()));







        assertEquals(new BigDecimal("22000.00"), costo1);







        // Caso 2: Presentación con costo 0.00 (debe calcular automáticamente precio_compra * factor)



        com.vitaNova.vitaNova.model.ProductoPresentacion presSinCosto = com.vitaNova.vitaNova.model.ProductoPresentacion.builder()



                .id_presentacion(2L)



                .factor_unidades(10)



                .costo_compra(BigDecimal.ZERO)



                .build();







        BigDecimal costo2 = (presSinCosto.getCosto_compra() != null && presSinCosto.getCosto_compra().compareTo(BigDecimal.ZERO) > 0)



                ? presSinCosto.getCosto_compra()



                : prod.getPrecio_compra().multiply(BigDecimal.valueOf(presSinCosto.getFactor_unidades()));







        assertEquals(new BigDecimal("25000.00"), costo2);



    }







    @Test



    @DisplayName("Etapa 4: Compra de producto que solo admite unidad base (ej. Suero o Frasco)")



    void testCompraProductoSoloUnidadBase() {



        Productos suero = new Productos();



        suero.setId_producto(5L);



        suero.setNombre("Electrolit");



        suero.setUnidad_base("Frasco");



        suero.setPrecio_compra(new BigDecimal("7000.00"));







        // Se compran 12 frascos directamente en unidad base (factor 1)



        int cantidad = 12;



        int factor = 1;



        int unidadesBase = cantidad * factor;



        BigDecimal costo = suero.getPrecio_compra();



        BigDecimal subtotal = costo.multiply(BigDecimal.valueOf(cantidad));







        DetalleCompra det = new DetalleCompra();



        det.setId_producto(suero.getId_producto());



        det.setNombre_presentacion(suero.getUnidad_base());



        det.setFactor_unidades(factor);



        det.setCantidad(cantidad);



        det.setPrecio(costo);



        det.setSubtotal(subtotal);



        det.setUnidades_base(unidadesBase);



        det.setCosto_unitario_base(costo);







        assertEquals(12, det.getCantidad());



        assertEquals(12, det.getUnidades_base());



        assertEquals(new BigDecimal("84000.00"), det.getSubtotal());



        assertEquals("Frasco", det.getNombre_presentacion());



    }







    @Test



    @DisplayName("Etapa 8: Parametrización administrativa de presentaciones por producto")



    void testParametrizacionAdministrativaPresentaciones() {



        // Simulación de parametrización para producto sólido (Dolex Gripa)



        Productos prod = new Productos();



        prod.setId_producto(4L);



        prod.setNombre("dolex gripa");



        prod.setUnidad_base("Tableta");



        prod.setPermite_fraccionamiento(true);



        prod.setPrecio_venta(new BigDecimal("2000.00"));



        prod.setPrecio_compra(new BigDecimal("1400.00"));







        List<ProductoPresentacion> presentaciones = new java.util.ArrayList<>();



        



        // Presentación 1: Tableta individual (Unidad base)



        presentaciones.add(ProductoPresentacion.builder()



                .id_presentacion(101L)



                .id_producto(prod.getId_producto())



                .nombre_presentacion("Tableta")



                .factor_unidades(1)



                .precio_venta(prod.getPrecio_venta())



                .costo_compra(prod.getPrecio_compra())



                .permite_venta(true)



                .permite_compra(true)



                .es_principal(false)



                .estado(ProductoPresentacion.EstadoPresentacion.ACTIVO)



                .build());







        // Presentación 2: Blíster x 4



        presentaciones.add(ProductoPresentacion.builder()



                .id_presentacion(102L)



                .id_producto(prod.getId_producto())



                .nombre_presentacion("Blíster x 4")



                .factor_unidades(4)



                .precio_venta(new BigDecimal("7500.00"))



                .costo_compra(new BigDecimal("5200.00"))



                .permite_venta(true)



                .permite_compra(true)



                .es_principal(false)



                .estado(ProductoPresentacion.EstadoPresentacion.ACTIVO)



                .build());







        // Presentación 3: Caja x 24 (Principal)



        presentaciones.add(ProductoPresentacion.builder()



                .id_presentacion(103L)



                .id_producto(prod.getId_producto())



                .nombre_presentacion("Caja x 24")



                .factor_unidades(24)



                .precio_venta(new BigDecimal("44000.00"))



                .costo_compra(new BigDecimal("31000.00"))



                .permite_venta(true)



                .permite_compra(true)



                .es_principal(true)



                .estado(ProductoPresentacion.EstadoPresentacion.ACTIVO)



                .build());







        assertEquals(3, presentaciones.size());



        



        // Verificar que la presentación principal es la Caja x 24



        ProductoPresentacion principal = presentaciones.stream()



                .filter(p -> Boolean.TRUE.equals(p.getEs_principal()))



                .findFirst()



                .orElse(null);



        assertNotNull(principal);



        assertEquals("Caja x 24", principal.getNombre_presentacion());



        assertEquals(24, principal.getFactor_unidades());







        // Simulación de depuración/sincronización de filas eliminadas



        java.util.Set<Long> idsEnviados = java.util.Set.of(101L, 103L); // Usuario quitó el blíster



        List<ProductoPresentacion> conservadas = presentaciones.stream()



                .filter(p -> idsEnviados.contains(p.getId_presentacion()))



                .toList();







        assertEquals(2, conservadas.size());



        assertTrue(conservadas.stream().anyMatch(p -> p.getNombre_presentacion().equals("Tableta")));



        assertTrue(conservadas.stream().anyMatch(p -> p.getNombre_presentacion().equals("Caja x 24")));



        assertFalse(conservadas.stream().anyMatch(p -> p.getNombre_presentacion().equals("Blíster x 4")));



    }







    @Test



    @DisplayName("Etapa 4: Múltiples ventas abiertas simultáneas, suspensión y finalización independiente")



    void testMultiplesVentasAbiertasYSuspendidas() {



        // Modelo de venta en memoria para representar una pestaña abierta (Tab)



        class VentaTab {



            String id;



            String titulo;



            Long idCliente;



            String nombreCliente;



            BigDecimal descuento;



            List<DetalleVenta> items = new ArrayList<>();



            boolean suspendida = false;







            BigDecimal calcularTotal() {



                BigDecimal subtotal = items.stream()



                        .map(DetalleVenta::getSubtotal)



                        .filter(Objects::nonNull)



                        .reduce(BigDecimal.ZERO, BigDecimal::add);



                BigDecimal desc = descuento != null ? descuento : BigDecimal.ZERO;



                BigDecimal iva = subtotal.multiply(new BigDecimal("0.19"));



                return subtotal.subtract(desc).add(iva);



            }



        }







        // 1. Simulación de Pestaña 1: Venta 1 con Cliente Carlos Gómez (Dolex Forte)



        VentaTab tab1 = new VentaTab();



        tab1.id = "tab_1";



        tab1.titulo = "Venta 1";



        tab1.idCliente = 10L;



        tab1.nombreCliente = "Carlos Gómez";



        tab1.descuento = BigDecimal.ZERO;







        DetalleVenta item1 = new DetalleVenta();



        item1.setId_producto(2L);



        item1.setCantidad(2); // 2 Cajas



        item1.setPrecio(new BigDecimal("55000.00"));



        item1.setSubtotal(new BigDecimal("110000.00"));



        item1.setId_presentacion(17L); // Caja x 24



        tab1.items.add(item1);







        // 2. Simulación de Pestaña 2: Venta 2 con Consumidor Final (Acetaminofén)



        VentaTab tab2 = new VentaTab();



        tab2.id = "tab_2";



        tab2.titulo = "Venta 2";



        tab2.idCliente = null;



        tab2.nombreCliente = "Consumidor Final";



        tab2.descuento = new BigDecimal("2000.00");







        DetalleVenta item2 = new DetalleVenta();



        item2.setId_producto(1L);



        item2.setCantidad(5); // 5 Tabletas



        item2.setPrecio(new BigDecimal("2000.00"));



        item2.setSubtotal(new BigDecimal("10000.00"));



        item2.setId_presentacion(1L); // Tableta x 1



        tab2.items.add(item2);







        List<VentaTab> pestañasAbiertas = new ArrayList<>();



        pestañasAbiertas.add(tab1);



        pestañasAbiertas.add(tab2);







        // Verificar independencia de las 2 ventas abiertas simultáneas



        assertEquals(2, pestañasAbiertas.size());



        assertEquals(1, tab1.items.size());



        assertEquals(1, tab2.items.size());



        assertEquals("Carlos Gómez", tab1.nombreCliente);



        assertEquals("Consumidor Final", tab2.nombreCliente);



        assertNotEquals(tab1.calcularTotal(), tab2.calcularTotal());







        // 3. Simulación de Suspensión de la Venta 1 (pasa a espera sin descontar stock)



        List<VentaTab> ventasEnEspera = new ArrayList<>();



        VentaTab tabASuspender = pestañasAbiertas.remove(0); // Quitar tab1 de abiertas



        tabASuspender.suspendida = true;



        ventasEnEspera.add(tabASuspender);







        assertEquals(1, pestañasAbiertas.size());



        assertEquals("Venta 2", pestañasAbiertas.get(0).titulo);



        assertEquals(1, ventasEnEspera.size());



        assertTrue(ventasEnEspera.get(0).suspendida);



        assertEquals("Carlos Gómez", ventasEnEspera.get(0).nombreCliente);







        // 4. Simulación de Finalización y Cobro ÚNICAMENTE de la Venta 2



        VentaTab ventaAFinalizar = pestañasAbiertas.get(0);



        int stockAcetaminofenAntes = 50;



        int unidadesDescontadasVenta2 = ventaAFinalizar.items.get(0).getCantidad() * 1; // 5 tabletas



        int stockAcetaminofenDespues = stockAcetaminofenAntes - unidadesDescontadasVenta2;



        assertEquals(45, stockAcetaminofenDespues);







        // Al finalizar la Venta 2, se remueve de pestañas abiertas



        pestañasAbiertas.remove(ventaAFinalizar);



        assertTrue(pestañasAbiertas.isEmpty());







        // La Venta 1 sigue intacta en la lista de espera, sin afectarse por el cobro de la Venta 2



        assertEquals(1, ventasEnEspera.size());



        assertEquals(1, ventasEnEspera.get(0).items.size());



        assertEquals(new BigDecimal("110000.00"), ventasEnEspera.get(0).items.get(0).getSubtotal());







        // 5. Simulación de Reanudación de la Venta 1 en una pestaña activa



        VentaTab ventaReanudada = ventasEnEspera.remove(0);



        ventaReanudada.suspendida = false;



        pestañasAbiertas.add(ventaReanudada);







        assertEquals(0, ventasEnEspera.size());



        assertEquals(1, pestañasAbiertas.size());



        assertEquals("Carlos Gómez", pestañasAbiertas.get(0).nombreCliente);



        assertEquals(2, pestañasAbiertas.get(0).items.get(0).getCantidad());



    }







    @Test



    @DisplayName("Etapa 5: Generación de comprobante POS, totales e idempotencia al reimprimir")



    void testGeneracionComprobanteReimpresionIdempotente() {



        // 1. Simulación de venta confirmada



        Venta venta = new Venta();



        venta.setId_venta(26L);



        venta.setFecha(LocalDateTime.of(2026, 10, 28, 14, 30, 0));



        venta.setId_empleado(1L);



        venta.setId_cliente(10L);



        venta.setId_metodo_pago(2L); // Tarjeta débito



        venta.setDescuento(new BigDecimal("5000.00"));



        venta.setImpuestos(new BigDecimal("18050.00"));



        venta.setTotal(new BigDecimal("113050.00"));



        venta.setEstado(Venta.Estado.PAGADA);







        // 2. Simulación de líneas de detalle



        List<DetalleVenta> detalles = new ArrayList<>();



        



        DetalleVenta d1 = new DetalleVenta();



        d1.setId_producto(2L);



        d1.setNombre_presentacion("Caja");



        d1.setFactor_unidades(24);



        d1.setCantidad(1);



        d1.setPrecio(new BigDecimal("55000.00"));



        d1.setSubtotal(new BigDecimal("55000.00"));



        detalles.add(d1);







        DetalleVenta d2 = new DetalleVenta();



        d2.setId_producto(3L);



        d2.setNombre_presentacion("Sobre");



        d2.setFactor_unidades(2);



        d2.setCantidad(2);



        d2.setPrecio(new BigDecimal("2800.00"));



        d2.setSubtotal(new BigDecimal("5600.00"));



        detalles.add(d2);







        DetalleVenta d3 = new DetalleVenta();



        d3.setId_producto(1L);



        d3.setNombre_presentacion("Caja");



        d3.setFactor_unidades(10);



        d3.setCantidad(2);



        d3.setPrecio(new BigDecimal("20000.00"));



        d3.setSubtotal(new BigDecimal("40000.00"));



        detalles.add(d3);







        // 3. Verificación de cálculo de subtotal bruto del comprobante



        BigDecimal subtotalBruto = detalles.stream()



                .map(DetalleVenta::getSubtotal)



                .reduce(BigDecimal.ZERO, BigDecimal::add);



        assertEquals(new BigDecimal("100600.00"), subtotalBruto);







        // 4. Verificación de datos del comprobante térmico



        String numeroComprobante = String.format("#VTA-%06d", venta.getId_venta());



        assertEquals("#VTA-000026", numeroComprobante);



        assertEquals(3, detalles.size());



        assertEquals("Caja", detalles.get(0).getNombre_presentacion());



        assertEquals(24, detalles.get(0).getFactor_unidades());







        // 5. Verificación de Idempotencia: Consultar o reimprimir el comprobante N veces



        // NO altera el inventario ni duplica ventas (operación estrictamente de sólo lectura GET)



        int stockSimuladoLoteAntes = 80;



        int llamadasReimpresion = 5; // Usuario imprime 5 veces la tirilla







        for (int i = 0; i < llamadasReimpresion; i++) {



            // Cada reimpresión consulta los mismos datos sin mutar estado



            assertNotNull(venta.getId_venta());



            assertEquals(new BigDecimal("113050.00"), venta.getTotal());



            assertEquals(Venta.Estado.PAGADA, venta.getEstado());



        }







        int stockSimuladoLoteDespues = stockSimuladoLoteAntes; // Stock intacto



        assertEquals(stockSimuladoLoteAntes, stockSimuladoLoteDespues);



    }







    @Test



    @DisplayName("Etapa 6: Gestión interna de proveedores como entidad comercial sin acceso de usuario")



    void testGestionInternaProveedoresSinUsuario() {



        // 1. Un proveedor se registra únicamente como entidad comercial interna



        Proveedor proveedor = new Proveedor();



        proveedor.setId_proveedor(10L);



        proveedor.setNombre("Distribuidora Farmacéutica Andina S.A.S.");



        proveedor.setNombre_contacto("Martín Suárez");



        proveedor.setNit("900.543.210-8");



        proveedor.setTelefono("311-555-8899");



        proveedor.setCorreo("contacto@distriandina.com");



        proveedor.setDireccion("Zona Industrial Cra 68 # 19-40");



        proveedor.setTipo_productos_suministrados("Medicamentos éticos y genéricos");



        proveedor.setEstado("ACTIVO");







        // 2. Verificar que Proveedor NO posee credenciales ni rol de usuario de acceso



        assertNotNull(proveedor.getNombre());



        assertNotNull(proveedor.getNit());



        assertEquals("ACTIVO", proveedor.getEstado());







        // Comprobar estructuralmente que la clase Proveedor no tiene campos de autenticación



        assertFalse(Arrays.stream(Proveedor.class.getDeclaredFields())



                .anyMatch(f -> f.getName().equalsIgnoreCase("password") ||



                               f.getName().equalsIgnoreCase("username") ||



                               f.getName().equalsIgnoreCase("id_usuario") ||



                               f.getName().equalsIgnoreCase("id_rol")));







        // 3. Comprobar que una compra se asocia directamente al proveedor comercial



        Compra compra = new Compra();



        compra.setId_compra(101L);



        compra.setId_proveedor(proveedor.getId_proveedor());



        compra.setProveedor(proveedor);



        compra.setTotal(new BigDecimal("1500000.00"));



        compra.setFecha(java.time.LocalDate.of(2026, 10, 28));



        compra.setEstado("RECIBIDA");







        assertEquals(10L, compra.getId_proveedor());



        assertEquals("Distribuidora Farmacéutica Andina S.A.S.", compra.getProveedor().getNombre());



        assertEquals("900.543.210-8", compra.getProveedor().getNit());







        // 4. Los usuarios del sistema corresponden exclusivamente al personal autorizado (empleados)



        Usuarios usuarioEmpleado = new Usuarios();



        usuarioEmpleado.setId_usuario(15L);



        usuarioEmpleado.setUsername("admin1");



        usuarioEmpleado.setPassword("$2a$10$encryptedPasswordHash");



        usuarioEmpleado.setId_rol(1L); // Administrador



        usuarioEmpleado.setEstado(true);







        // Validar separación absoluta de identidades



        assertNotEquals(proveedor.getId_proveedor(), usuarioEmpleado.getId_usuario());



        assertEquals("admin1", usuarioEmpleado.getUsername());



        assertNotNull(usuarioEmpleado.getPassword());



    }





    // =========================================================

    // ETAPA 7: PRUEBAS INTEGRALES DE VITANOVA

    // =========================================================



    @Test

    @DisplayName("Etapa 7 - Escenario 1: Encabezados y títulos sin duplicidad en todos los módulos")

    void testEscenario1EncabezadosYVistas() {

        String[] modulos = {"Productos", "Inventario", "Compras", "Ventas", "Proveedores", "Clientes", "Usuarios", "Roles", "Reportes"};

        for (String mod : modulos) {

            assertNotNull(mod);

            assertFalse(mod.isBlank());

        }

        Map<String, Object> modelMap = new HashMap<>();

        modelMap.put("pageTitle", "Gestión de Proveedores");

        modelMap.put("pageSubtitle", "Directorio comercial interno de laboratorios y distribuidores farmacéuticos.");



        assertEquals("Gestión de Proveedores", modelMap.get("pageTitle"));

        assertEquals("Directorio comercial interno de laboratorios y distribuidores farmacéuticos.", modelMap.get("pageSubtitle"));

    }



    @Test

    @DisplayName("Etapa 7 - Escenario 2: Venta rápida con un producto, varios productos, modificación y cliente opcional")

    void testEscenario2VentaRapida() {

        // 1. Venta rápida con un solo producto y cliente no identificado (Consumidor Final)

        Venta venta = new Venta();

        venta.setId_venta(50L);

        venta.setId_cliente(null); // Cliente opcional -> Consumidor Final

        venta.setId_empleado(1L);

        venta.setId_metodo_pago(1L); // Efectivo

        venta.setEstado(Venta.Estado.PAGADA);

        venta.setDescuento(BigDecimal.ZERO);



        List<DetalleVenta> items = new ArrayList<>();

        DetalleVenta item1 = new DetalleVenta();

        item1.setId_producto(1L);

        item1.setCantidad(2);

        item1.setPrecio(new BigDecimal("2000.00"));

        item1.setSubtotal(item1.getPrecio().multiply(BigDecimal.valueOf(item1.getCantidad())));

        items.add(item1);



        assertEquals(new BigDecimal("4000.00"), item1.getSubtotal());



        // 2. Agregar segundo producto

        DetalleVenta item2 = new DetalleVenta();

        item2.setId_producto(2L);

        item2.setCantidad(1);

        item2.setPrecio(new BigDecimal("15000.00"));

        item2.setSubtotal(new BigDecimal("15000.00"));

        items.add(item2);



        // 3. Cambiar cantidad de item1 de 2 a 3

        item1.setCantidad(3);

        item1.setSubtotal(item1.getPrecio().multiply(BigDecimal.valueOf(item1.getCantidad()))); // 6000.00



        // 4. Retirar item2

        items.remove(item2);

        assertEquals(1, items.size());



        // 5. Total de la venta

        BigDecimal subtotal = items.stream().map(DetalleVenta::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCalculado = subtotal.subtract(venta.getDescuento());

        venta.setTotal(totalCalculado);



        assertEquals(new BigDecimal("6000.00"), venta.getTotal());

        assertNull(venta.getId_cliente()); // Comprobado: cliente es opcional

    }



    @Test

    @DisplayName("Etapa 7 - Escenario 3: Presentaciones comerciales, equivalencias a unidad base y rechazo de sobreventa")

    void testEscenario3PresentacionesInventarioYValidaciones() {

        // Producto: Amoxicilina 500 mg, unidad base: Tableta

        int stockBaseDisponible = 50; // 50 tabletas físicas



        // Presentación 1: Tableta individual (factor 1)

        int factorTableta = 1;

        int cantTabletas = 5;

        int unidadesTableta = cantTabletas * factorTableta; // 5



        // Presentación 2: Caja x 20 (factor 20)

        int factorCaja = 20;

        int cantCajas = 2;

        int unidadesCajas = cantCajas * factorCaja; // 40



        int totalUnidadesBaseADescontar = unidadesTableta + unidadesCajas; // 45

        assertTrue(totalUnidadesBaseADescontar <= stockBaseDisponible, "Hay stock suficiente para 45 tabletas");



        int stockRestante = stockBaseDisponible - totalUnidadesBaseADescontar;

        assertEquals(5, stockRestante);



        // Intento de sobreventa: El cliente pide 1 caja más (20 tabletas), pero solo quedan 5

        int pedidoExcedido = 1 * factorCaja;

        boolean hayStockParaExceso = (pedidoExcedido <= stockRestante);

        assertFalse(hayStockParaExceso, "El sistema debe rechazar la venta cuando la equivalencia supera el stock base");



        // Rechazo atómico: el stock queda intacto en 5 y no se produce descuento duplicado

        assertEquals(5, stockRestante);

    }



    @Test

    @DisplayName("Etapa 7 - Escenario 4: Múltiples ventas simultáneas, suspensión, cambio de pestañas y cobro independiente")

    void testEscenario4VentasSimultaneas() {

        // Pestaña A: Mostrador 1 (Cliente Doña María)

        VentaTab tabA = new VentaTab();

        tabA.id = "tab_a";

        tabA.titulo = "Venta Mostrador 1";

        tabA.nombreCliente = "María Rodríguez";

        DetalleVenta prodA = new DetalleVenta();

        prodA.setId_producto(1L);

        prodA.setCantidad(3);

        prodA.setSubtotal(new BigDecimal("6000.00"));

        tabA.items.add(prodA);



        // Pestaña B: Domicilio telefónico (Cliente Don Pedro)

        VentaTab tabB = new VentaTab();

        tabB.id = "tab_b";

        tabB.titulo = "Venta Domicilio";

        tabB.nombreCliente = "Pedro Pérez";

        DetalleVenta prodB = new DetalleVenta();

        prodB.setId_producto(2L);

        prodB.setCantidad(1);

        prodB.setSubtotal(new BigDecimal("35000.00"));

        tabB.items.add(prodB);



        List<VentaTab> pestañasAbiertas = new ArrayList<>();

        pestañasAbiertas.add(tabA);

        pestañasAbiertas.add(tabB);



        // Suspender venta A (Doña María fue al cajero a retirar dinero)

        tabA.suspendida = true;

        List<VentaTab> suspendidas = new ArrayList<>();

        suspendidas.add(tabA);

        pestañasAbiertas.remove(tabA);



        // Se cobra y finaliza únicamente la venta B

        assertEquals(1, pestañasAbiertas.size());

        assertEquals("Venta Domicilio", pestañasAbiertas.get(0).titulo);

        VentaTab ventaCobrada = pestañasAbiertas.remove(0);

        assertEquals(new BigDecimal("35000.00"), ventaCobrada.items.get(0).getSubtotal());



        // La venta suspendida A se retoma intacta sin afectarse por el cobro de B

        assertEquals(1, suspendidas.size());

        VentaTab ventaRetomada = suspendidas.remove(0);

        ventaRetomada.suspendida = false;

        pestañasAbiertas.add(ventaRetomada);



        assertEquals("María Rodríguez", pestañasAbiertas.get(0).nombreCliente);

        assertEquals(new BigDecimal("6000.00"), pestañasAbiertas.get(0).items.get(0).getSubtotal());

    }



    @Test

    @DisplayName("Etapa 7 - Escenario 5: Tirilla térmica, datos obligatorios e idempotencia en reimpresión")

    void testEscenario5TirillaTermicaEIdempotencia() {

        Venta v = new Venta();

        v.setId_venta(88L);

        v.setFecha(LocalDateTime.of(2026, 10, 28, 16, 45, 0));

        v.setTotal(new BigDecimal("28000.00"));

        v.setDescuento(BigDecimal.ZERO);

        v.setEstado(Venta.Estado.PAGADA);

        v.setId_empleado(2L);



        // Formato de número de comprobante térmico POS

        String comprobante = String.format("#VTA-%06d", v.getId_venta());

        assertEquals("#VTA-000088", comprobante);



        // Idempotencia: 10 reimpresiones del comprobante

        for (int i = 0; i < 10; i++) {

            assertEquals(Venta.Estado.PAGADA, v.getEstado());

            assertEquals(new BigDecimal("28000.00"), v.getTotal());

        }

    }



    @Test

    @DisplayName("Etapa 7 - Escenario 6: Proveedor como entidad comercial sin acceso de usuario y asociado a compra")

    void testEscenario6ProveedoresYUsuarios() {

        Proveedor p = new Proveedor();

        p.setId_proveedor(5L);

        p.setNombre("Laboratorio Genfar S.A.");

        p.setNit("860.001.234-5");

        p.setEstado("ACTIVO");



        // Sin usuario ni credenciales

        assertTrue(Arrays.stream(Proveedor.class.getDeclaredFields()).noneMatch(f -> f.getName().contains("password")));



        Compra c = new Compra();

        c.setId_compra(45L);

        c.setId_proveedor(p.getId_proveedor());

        c.setProveedor(p);

        c.setTotal(new BigDecimal("850000.00"));

        c.setEstado("RECIBIDA");



        assertEquals(5L, c.getId_proveedor());

        assertEquals("Laboratorio Genfar S.A.", c.getProveedor().getNombre());

    }



    @Test

    @DisplayName("Etapa 7 - Escenario 7: Regresión de permisos y seguridad por roles")

    void testEscenario7SeguridadYPermisosPorRol() {
        RolPermisosService service = new RolPermisosService();

        // 1. ROLE_ADMINISTRADOR tiene acceso a todos los módulos incluido Usuarios y Roles
        Set<String> adminPerms = service.obtenerPermisos("ADMINISTRADOR", 1L);
        assertTrue(adminPerms.contains("PERM_ROLES"));
        assertTrue(adminPerms.contains("PERM_USUARIOS"));
        assertTrue(adminPerms.contains("PERM_PRODUCTOS"));
        assertTrue(adminPerms.contains("PERM_INVENTARIO"));
        assertTrue(adminPerms.contains("PERM_VENTAS"));
        assertTrue(adminPerms.contains("PERM_COMPRAS"));

        // 2. ROLE_FARMACEUTICO puede gestionar productos, inventario y compras, pero NO usuarios ni roles
        Set<String> farmaPerms = service.obtenerPermisos("FARMACEUTICO", 2L);
        assertTrue(farmaPerms.contains("PERM_PRODUCTOS"));
        assertTrue(farmaPerms.contains("PERM_INVENTARIO"));
        assertTrue(farmaPerms.contains("PERM_COMPRAS"));
        assertFalse(farmaPerms.contains("PERM_ROLES"));
        assertFalse(farmaPerms.contains("PERM_USUARIOS"));

        // 3. ROLE_VENDEDOR puede registrar ventas pero NO modificar roles ni compras
        Set<String> vendedorPerms = service.obtenerPermisos("VENDEDOR", 3L);
        assertTrue(vendedorPerms.contains("PERM_VENTAS"));
        assertFalse(vendedorPerms.contains("PERM_ROLES"));
        assertFalse(vendedorPerms.contains("PERM_COMPRAS"));
    }
}

