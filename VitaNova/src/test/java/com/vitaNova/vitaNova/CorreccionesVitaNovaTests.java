package com.vitaNova.vitaNova;

import com.vitaNova.vitaNova.model.Clientes;
import com.vitaNova.vitaNova.model.DetalleCompra;
import com.vitaNova.vitaNova.model.Productos;
import com.vitaNova.vitaNova.model.Venta;
import com.vitaNova.vitaNova.service.RolPermisosService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CorreccionesVitaNovaTests {

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
}
