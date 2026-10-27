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
}
