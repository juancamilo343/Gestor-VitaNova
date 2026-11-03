package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Productos;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import com.vitaNova.vitaNova.repository.CategoriaRepository;
import com.vitaNova.vitaNova.repository.ProveedorRepository;
import com.vitaNova.vitaNova.model.ProductoPresentacion;
import com.vitaNova.vitaNova.repository.ProductoPresentacionRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/view/productos")
public class ProductosView {

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ProductoPresentacionRepository productoPresentacionRepository;


    // ==========================================================
    // LISTA DE PRODUCTOS
    // TODOS LOS ROLES PUEDEN VISUALIZAR
    // ==========================================================
    @GetMapping
    public String lista(
            Model model,
            Authentication authentication
    ) {

        model.addAttribute("productos", productosRepository.findAll());

        model.addAttribute("activeMenu", "productos");
        model.addAttribute("userName", "Administrador");
        model.addAttribute("userRole", "Farmacia Central");

        model.addAttribute(
                "pageTitle",
                "Gestión de Productos"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de productos de la farmacia."
        );

        model.addAttribute(
                "puedeModificar",
                puedeModificar(authentication)
        );

        return "productos/productos";
    }


    // ==========================================================
    // FORMULARIO NUEVO PRODUCTO
    // SOLO ADMINISTRADOR Y FARMACÉUTICO
    // ==========================================================
    @GetMapping("/form")
    public String form(
            Model model,
            Authentication authentication,
            RedirectAttributes ra
    ) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para crear productos."
            );

            return "redirect:/view/productos";
        }

        Productos producto = new Productos();

        producto.setEstado("ACTIVO");
        producto.setPrecio_compra(BigDecimal.ZERO);
        producto.setPrecio_venta(BigDecimal.ZERO);

        model.addAttribute(
                "producto",
                producto
        );

        model.addAttribute(
                "categorias",
                categoriaRepository.findAll()
        );

        model.addAttribute(
                "proveedores",
                proveedorRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Nuevo Producto"
        );

        model.addAttribute(
                "pageSubtitle",
                "Registre un nuevo producto dentro del sistema VitaNova."
        );

        producto.setUnidad_base("Unidad");
        producto.setPermite_fraccionamiento(false);

        model.addAttribute(
                "presentaciones",
                Collections.emptyList()
        );

        model.addAttribute(
                "editMode",
                false
        );

        return "productos/productosForm";
    }


    // ==========================================================
    // EDITAR PRODUCTO
    // SOLO ADMINISTRADOR Y FARMACÉUTICO
    // ==========================================================
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            Authentication authentication,
            RedirectAttributes ra
    ) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para editar productos."
            );

            return "redirect:/view/productos";
        }

        Productos producto =
                productosRepository.findById(id).orElse(null);

        if (producto == null) {

            ra.addFlashAttribute(
                    "error",
                    "Producto no encontrado."
            );

            return "redirect:/view/productos";
        }

        model.addAttribute(
                "producto",
                producto
        );

        model.addAttribute(
                "categorias",
                categoriaRepository.findAll()
        );

        model.addAttribute(
                "proveedores",
                proveedorRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Editar Producto"
        );

        model.addAttribute(
                "pageSubtitle",
                "Actualice la información del producto seleccionado."
        );

        List<ProductoPresentacion> presentaciones =
                productoPresentacionRepository.findPorProducto(id);

        model.addAttribute(
                "presentaciones",
                presentaciones
        );

        model.addAttribute(
                "editMode",
                true
        );

        return "productos/productosForm";
    }


    // ==========================================================
    // GUARDAR PRODUCTO
    // SOLO ADMINISTRADOR Y FARMACÉUTICO
    // ==========================================================
    @PostMapping("/save")
    @Transactional
    public String save(
            @ModelAttribute Productos producto,
            @RequestParam(value = "pres_id[]", required = false) List<Long> presIds,
            @RequestParam(value = "pres_nombre[]", required = false) List<String> presNombres,
            @RequestParam(value = "pres_factor[]", required = false) List<Integer> presFactores,
            @RequestParam(value = "pres_precio_venta[]", required = false) List<BigDecimal> presPreciosVenta,
            @RequestParam(value = "pres_costo_compra[]", required = false) List<BigDecimal> presCostosCompra,
            @RequestParam(value = "pres_permite_venta[]", required = false) List<String> presPermiteVenta,
            @RequestParam(value = "pres_permite_compra[]", required = false) List<String> presPermiteCompra,
            @RequestParam(value = "pres_es_principal", required = false) Integer presPrincipalIdx,
            @RequestParam(value = "pres_estado[]", required = false) List<String> presEstados,
            Authentication authentication,
            RedirectAttributes ra
    ) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para modificar productos."
            );

            return "redirect:/view/productos";
        }

        boolean nuevo =
                producto.getId_producto() == null;

        if (producto.getEstado() == null
                || producto.getEstado().isBlank()) {

            producto.setEstado("ACTIVO");
        }

        if (producto.getUnidad_base() == null || producto.getUnidad_base().isBlank()) {
            producto.setUnidad_base("Unidad");
        }

        if (producto.getPermite_fraccionamiento() == null) {
            producto.setPermite_fraccionamiento(false);
        }

        Productos productoGuardado = productosRepository.save(producto);

        // Procesar presentaciones comerciales
        if (presNombres != null && !presNombres.isEmpty()) {
            boolean tienePrincipal = false;
            java.util.Set<Long> idsGuardados = new java.util.HashSet<>();
            for (int i = 0; i < presNombres.size(); i++) {
                String nombre = presNombres.get(i) != null ? presNombres.get(i).trim() : "";
                if (nombre.isBlank()) {
                    continue;
                }

                int factor = (presFactores != null && i < presFactores.size() && presFactores.get(i) != null && presFactores.get(i) > 0)
                        ? presFactores.get(i) : 1;

                BigDecimal pVenta = (presPreciosVenta != null && i < presPreciosVenta.size() && presPreciosVenta.get(i) != null)
                        ? presPreciosVenta.get(i)
                        : (productoGuardado.getPrecio_venta() != null ? productoGuardado.getPrecio_venta() : BigDecimal.ZERO);

                BigDecimal cCompra = (presCostosCompra != null && i < presCostosCompra.size() && presCostosCompra.get(i) != null)
                        ? presCostosCompra.get(i)
                        : (productoGuardado.getPrecio_compra() != null ? productoGuardado.getPrecio_compra() : BigDecimal.ZERO);

                boolean pVentaFlag = presPermiteVenta == null || i >= presPermiteVenta.size() || "true".equalsIgnoreCase(presPermiteVenta.get(i));
                boolean pCompraFlag = presPermiteCompra == null || i >= presPermiteCompra.size() || "true".equalsIgnoreCase(presPermiteCompra.get(i));
                boolean esPrincipal = (presPrincipalIdx != null && presPrincipalIdx == i);
                if (esPrincipal) tienePrincipal = true;

                String estadoStr = (presEstados != null && i < presEstados.size() && "INACTIVO".equalsIgnoreCase(presEstados.get(i)))
                        ? "INACTIVO" : "ACTIVO";

                Long presId = (presIds != null && i < presIds.size()) ? presIds.get(i) : null;

                ProductoPresentacion pres = null;
                if (presId != null) {
                    pres = productoPresentacionRepository.findById(presId).orElse(null);
                }
                if (pres == null) {
                    pres = new ProductoPresentacion();
                    pres.setId_producto(productoGuardado.getId_producto());
                }

                pres.setNombre_presentacion(nombre);
                pres.setFactor_unidades(factor);
                pres.setPrecio_venta(pVenta);
                pres.setCosto_compra(cCompra);
                pres.setPermite_venta(pVentaFlag);
                pres.setPermite_compra(pCompraFlag);
                pres.setEs_principal(esPrincipal);
                pres.setEstado(ProductoPresentacion.EstadoPresentacion.valueOf(estadoStr));

                ProductoPresentacion presGuardada = productoPresentacionRepository.save(pres);
                idsGuardados.add(presGuardada.getId_presentacion());
            }

            // Sincronizar y depurar presentaciones eliminadas en el formulario
            List<ProductoPresentacion> existentes = productoPresentacionRepository.findPorProducto(productoGuardado.getId_producto());
            for (ProductoPresentacion ex : existentes) {
                if (!idsGuardados.contains(ex.getId_presentacion())) {
                    try {
                        productoPresentacionRepository.delete(ex);
                    } catch (Exception e) {
                        ex.setEstado(ProductoPresentacion.EstadoPresentacion.INACTIVO);
                        ex.setPermite_compra(false);
                        ex.setPermite_venta(false);
                        productoPresentacionRepository.save(ex);
                    }
                }
            }

            // Asegurar que al menos una presentación sea principal si ninguna fue marcada
            if (!tienePrincipal) {
                List<ProductoPresentacion> activas = productoPresentacionRepository.findActivasPorProducto(productoGuardado.getId_producto());
                if (!activas.isEmpty()) {
                    ProductoPresentacion primera = activas.get(0);
                    primera.setEs_principal(true);
                    productoPresentacionRepository.save(primera);
                }
            }
        } else {
            // Si no se enviaron presentaciones, asegurar la presentación base automática
            List<ProductoPresentacion> existentes = productoPresentacionRepository.findPorProducto(productoGuardado.getId_producto());
            if (existentes.isEmpty()) {
                ProductoPresentacion base = ProductoPresentacion.builder()
                        .id_producto(productoGuardado.getId_producto())
                        .nombre_presentacion(productoGuardado.getUnidad_base())
                        .factor_unidades(1)
                        .precio_venta(productoGuardado.getPrecio_venta() != null ? productoGuardado.getPrecio_venta() : BigDecimal.ZERO)
                        .costo_compra(productoGuardado.getPrecio_compra() != null ? productoGuardado.getPrecio_compra() : BigDecimal.ZERO)
                        .es_principal(true)
                        .permite_venta(true)
                        .permite_compra(true)
                        .estado(ProductoPresentacion.EstadoPresentacion.ACTIVO)
                        .build();
                productoPresentacionRepository.save(base);
            }
        }

        if (nuevo) {

            ra.addFlashAttribute(
                    "success",
                    "Producto y presentaciones registrados con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Producto y presentaciones actualizados con éxito"
            );
        }

        return "redirect:/view/productos";
    }


    // ==========================================================
    // ELIMINAR PRODUCTO
    // SOLO ADMINISTRADOR Y FARMACÉUTICO
    // ==========================================================
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes ra
    ) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para eliminar productos."
            );

            return "redirect:/view/productos";
        }

        if (productosRepository.existsById(id)) {

            productosRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Producto eliminado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "error",
                    "Producto no encontrado"
            );
        }

        return "redirect:/view/productos";
    }


    // ==========================================================
    // VALIDAR PERMISOS DE MODIFICACIÓN
    // ADMINISTRADOR + FARMACÉUTICO
    // ==========================================================
    private boolean puedeModificar(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMINISTRADOR")
                                || authority.getAuthority().equals("ROLE_FARMACEUTICO")
                );
    }
}