package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Clientes;
import com.vitaNova.vitaNova.model.DetalleVenta;
import com.vitaNova.vitaNova.model.MetodoPago;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import com.vitaNova.vitaNova.model.DetalleVentaLote;
import com.vitaNova.vitaNova.model.Empleados;
import com.vitaNova.vitaNova.model.Inventario;
import com.vitaNova.vitaNova.model.Lote;
import com.vitaNova.vitaNova.model.Productos;
import com.vitaNova.vitaNova.model.Usuarios;
import com.vitaNova.vitaNova.model.Venta;
import com.vitaNova.vitaNova.repository.ClientesRepository;
import com.vitaNova.vitaNova.repository.DetalleVentaLoteRepository;
import com.vitaNova.vitaNova.repository.DetalleVentaRepository;
import com.vitaNova.vitaNova.repository.EmpleadosRepository;
import com.vitaNova.vitaNova.repository.InventarioRepository;
import com.vitaNova.vitaNova.repository.LoteRepository;
import com.vitaNova.vitaNova.repository.MetodoPagoRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import com.vitaNova.vitaNova.repository.UsuariosRepository;
import com.vitaNova.vitaNova.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/view/ventas")
public class VentaView {

    private static final BigDecimal PORCENTAJE_IMPUESTO =
            new BigDecimal("0.19");

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private DetalleVentaLoteRepository detalleVentaLoteRepository;

    @Autowired
    private ClientesRepository clientesRepository;

    @Autowired
    private EmpleadosRepository empleadosRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;


    // =========================================================
    // LISTAR VENTAS
    // =========================================================

    private Map<Long, String> cargarMapaClientes() {
        return clientesRepository.findAll().stream()
                .filter(c -> c.getId_cliente() != null)
                .collect(Collectors.toMap(
                        Clientes::getId_cliente,
                        c -> {
                            String nom = c.getNombres() != null ? c.getNombres().trim() : "";
                            String ape = c.getApellidos() != null && !c.getApellidos().isBlank() ? " " + c.getApellidos().trim() : "";
                            return (nom + ape).isBlank() ? "Cliente #" + c.getId_cliente() : (nom + ape);
                        },
                        (a, b) -> a
                ));
    }

    private Map<Long, String> cargarMapaEmpleados() {
        return empleadosRepository.findAll().stream()
                .filter(e -> e.getId_empleado() != null)
                .collect(Collectors.toMap(
                        Empleados::getId_empleado,
                        e -> {
                            String nom = e.getNombres() != null ? e.getNombres().trim() : "";
                            String ape = e.getApellidos() != null && !e.getApellidos().isBlank() ? " " + e.getApellidos().trim() : "";
                            return (nom + ape).isBlank() ? "Empleado #" + e.getId_empleado() : (nom + ape);
                        },
                        (a, b) -> a
                ));
    }

    private Map<Long, String> cargarMapaMetodosPago() {
        return metodoPagoRepository.findAll().stream()
                .filter(m -> m.getId_metodo_pago() != null && m.getNombre() != null)
                .collect(Collectors.toMap(
                        MetodoPago::getId_metodo_pago,
                        MetodoPago::getNombre,
                        (a, b) -> a
                ));
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fechaInicio,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fechaFin,
            @RequestParam(required = false) String estado,
            Model model,
            Authentication authentication) {

        Map<Long, String> nombresClientes = cargarMapaClientes();
        Map<Long, String> nombresEmpleados = cargarMapaEmpleados();
        Map<Long, String> nombresMetodosPago = cargarMapaMetodosPago();

        List<Venta> todas = ventaRepository.findAll();
        List<Venta> filtradas = todas.stream().filter(v -> {
            if (estado != null && !estado.isBlank() && !estado.equalsIgnoreCase("TODOS")) {
                if (v.getEstado() == null || !v.getEstado().name().equalsIgnoreCase(estado.trim())) {
                    return false;
                }
            }
            if (fechaInicio != null && v.getFecha() != null) {
                if (v.getFecha().toLocalDate().isBefore(fechaInicio)) {
                    return false;
                }
            }
            if (fechaFin != null && v.getFecha() != null) {
                if (v.getFecha().toLocalDate().isAfter(fechaFin)) {
                    return false;
                }
            }
            if (buscar != null && !buscar.isBlank()) {
                String b = buscar.trim().toLowerCase();
                String idStr = v.getId_venta() != null ? String.valueOf(v.getId_venta()) : "";
                String cliNombre = (v.getId_cliente() != null && nombresClientes.containsKey(v.getId_cliente()))
                        ? nombresClientes.get(v.getId_cliente()).toLowerCase() : "";
                if (!idStr.contains(b) && !cliNombre.contains(b)) {
                    return false;
                }
            }
            return true;
        }).sorted((v1, v2) -> {
            if (v1.getFecha() == null) return 1;
            if (v2.getFecha() == null) return -1;
            return v2.getFecha().compareTo(v1.getFecha());
        }).toList();

        BigDecimal totalRecaudado = filtradas.stream()
                .filter(v -> v.getEstado() != Venta.Estado.ANULADA && v.getTotal() != null)
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("ventas", filtradas);
        model.addAttribute("nombresClientes", nombresClientes);
        model.addAttribute("nombresEmpleados", nombresEmpleados);
        model.addAttribute("nombresMetodosPago", nombresMetodosPago);
        model.addAttribute("buscar", buscar != null ? buscar : "");
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("estadoSeleccionado", estado != null ? estado : "");
        model.addAttribute("totalVentas", filtradas.size());
        model.addAttribute("totalRecaudado", totalRecaudado);
        model.addAttribute("pageTitle", "Gestión de Ventas");
        model.addAttribute("pageSubtitle", "Historial y seguimiento de ventas de la farmacia.");
        model.addAttribute("activeMenu", "ventas");
        model.addAttribute("userName", "Administrador");
        model.addAttribute("userRole", "Farmacia Central");
        model.addAttribute("puedeModificar", puedeModificar(authentication));

        return "Ventas/ventas";
    }


    // =========================================================
    // FORMULARIO NUEVA VENTA
    // =========================================================

    @GetMapping("/form")
    public String form(
            Model model,
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para registrar ventas."
            );

            return "redirect:/view/ventas";
        }

        Venta venta = new Venta();

        venta.setFecha(LocalDateTime.now());

        // =====================================================
        // EMPLEADO AUTOMÁTICO SEGÚN USUARIO AUTENTICADO
        // =====================================================

        Empleados empleadoActual =
                obtenerEmpleadoAutenticado();

        venta.setId_empleado(
                empleadoActual.getId_empleado()
        );

        venta.setDescuento(BigDecimal.ZERO);
        venta.setImpuestos(BigDecimal.ZERO);
        venta.setTotal(BigDecimal.ZERO);
        venta.setEstado(Venta.Estado.PAGADA);

        cargarDatosFormulario(
                model,
                venta
        );

        model.addAttribute(
                "pageTitle",
                "Nueva venta"
        );

        model.addAttribute(
                "pageSubtitle",
                "Registre la venta, agregue los productos y confirme para descontar el inventario."
        );

        return "Ventas/ventasForm";
    }


    // =========================================================
    // GUARDAR VENTA CON DETALLES
    // =========================================================

    @PostMapping("/save")
    @Transactional
    public String guardar(
            @ModelAttribute Venta venta,
            @RequestParam(
                    value = "id_producto",
                    required = false
            ) List<Long> idProductos,
            @RequestParam(
                    value = "cantidad",
                    required = false
            ) List<Integer> cantidades,
            Model model,
            RedirectAttributes ra,
            Authentication authentication) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para registrar ventas."
            );

            return "redirect:/view/ventas";
        }

        venta.setFecha(LocalDateTime.now());

        // =====================================================
        // EMPLEADO AUTOMÁTICO
        // =====================================================

        Empleados empleadoActual =
                obtenerEmpleadoAutenticado();

        venta.setId_empleado(
                empleadoActual.getId_empleado()
        );

        if (venta.getDescuento() == null) {
            venta.setDescuento(BigDecimal.ZERO);
        }

        if (venta.getEstado() == null) {
            venta.setEstado(Venta.Estado.PAGADA);
        }

        List<DetalleVenta> lineas =
                construirLineas(
                        idProductos,
                        cantidades
                );

        String errorLineas =
                validarLineas(lineas);

        if (errorLineas != null) {

            return devolverFormularioConError(
                    model,
                    venta,
                    errorLineas
            );
        }

        String errorDisponibilidad =
                validarDisponibilidadCompleta(lineas);

        if (errorDisponibilidad != null) {

            return devolverFormularioConError(
                    model,
                    venta,
                    errorDisponibilidad
            );
        }

        venta.setImpuestos(BigDecimal.ZERO);
        venta.setTotal(BigDecimal.ZERO);

        Venta nuevaVenta =
                ventaRepository.save(venta);

        List<DetalleVenta> detallesGuardados =
                new ArrayList<>();

        for (DetalleVenta detalle : lineas) {

            detalle.setId_venta(
                    nuevaVenta.getId_venta()
            );

            DetalleVenta detalleGuardado =
                    detalleVentaRepository.save(
                            detalle
                    );

            detallesGuardados.add(
                    detalleGuardado
            );
        }

        if (!inventarioYaLiberado(
                nuevaVenta.getEstado()
        )) {

            descontarLotesEInventario(
                    detallesGuardados
            );
        }

        recalcularTotal(nuevaVenta);

        ra.addFlashAttribute(
                "success",
                "Venta realizada satisfactoriamente."
        );

        return "redirect:/view/ventas/"
                + nuevaVenta.getId_venta()
                + "/detalles";
    }


    // =========================================================
    // EDITAR VENTA
    // =========================================================

    @GetMapping("/edit/{id}")
    public String editar(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra,
            Authentication authentication) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para editar ventas."
            );

            return "redirect:/view/ventas";
        }

        Venta venta =
                ventaRepository
                        .findById(id)
                        .orElse(null);

        if (venta == null) {

            ra.addFlashAttribute(
                    "error",
                    "Venta no encontrada."
            );

            return "redirect:/view/ventas";
        }

        cargarDatosFormulario(
                model,
                venta
        );

        model.addAttribute(
                "pageTitle",
                "Editar venta"
        );

        model.addAttribute(
                "pageSubtitle",
                "Actualice la información de la venta."
        );

        return "Ventas/ventasForm";
    }


    // =========================================================
    // ACTUALIZAR VENTA
    // =========================================================

    @PostMapping("/update")
    @Transactional
    public String actualizar(
            @ModelAttribute Venta datos,
            RedirectAttributes ra,
            Authentication authentication) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para actualizar ventas."
            );

            return "redirect:/view/ventas";
        }

        if (datos.getId_venta() == null) {

            ra.addFlashAttribute(
                    "error",
                    "No se pudo identificar la venta."
            );

            return "redirect:/view/ventas";
        }

        Venta venta =
                ventaRepository
                        .findById(datos.getId_venta())
                        .orElse(null);

        if (venta == null) {

            ra.addFlashAttribute(
                    "error",
                    "Venta no encontrada."
            );

            return "redirect:/view/ventas";
        }

        Venta.Estado estadoAnterior =
                venta.getEstado();

        venta.setId_cliente(
                datos.getId_cliente()
        );

        venta.setId_metodo_pago(
                datos.getId_metodo_pago()
        );

        venta.setDescuento(
                datos.getDescuento() != null
                        ? datos.getDescuento()
                        : BigDecimal.ZERO
        );

        if (datos.getEstado() != null) {

            venta.setEstado(
                    datos.getEstado()
            );
        }

        if (!inventarioYaLiberado(estadoAnterior)
                && inventarioYaLiberado(
                venta.getEstado()
        )) {

            List<DetalleVenta> detalles =
                    detallesDeVenta(
                            venta.getId_venta()
                    );

            boolean tieneAsignaciones =
                    tieneAsignacionesDeLotes(
                            detalles
                    );

            if (tieneAsignaciones) {

                restaurarLotesDeVenta(
                        detalles
                );

                sincronizarInventarioConLotes(
                        detalles
                );

            } else {

                restaurarInventario(
                        detalles
                );
            }
        }

        recalcularTotal(venta);

        ra.addFlashAttribute(
                "success",
                "Venta actualizada correctamente."
        );

        return "redirect:/view/ventas";
    }


    // =========================================================
    // ANULAR VENTA
    // =========================================================

    @PostMapping("/anular/{id}")
    @Transactional
    public String anular(
            @PathVariable Long id,
            RedirectAttributes ra,
            Authentication authentication) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para anular ventas."
            );

            return "redirect:/view/ventas";
        }

        Venta venta =
                ventaRepository
                        .findById(id)
                        .orElse(null);

        if (venta == null) {

            ra.addFlashAttribute(
                    "error",
                    "Venta no encontrada."
            );

            return "redirect:/view/ventas";
        }

        if (inventarioYaLiberado(
                venta.getEstado()
        )) {

            ra.addFlashAttribute(
                    "error",
                    "La venta ya está anulada o devuelta. El inventario no se modifica otra vez."
            );

            return "redirect:/view/ventas";
        }

        List<DetalleVenta> detalles =
                detallesDeVenta(id);

        boolean tieneAsignaciones =
                tieneAsignacionesDeLotes(
                        detalles
                );

        if (tieneAsignaciones) {

            restaurarLotesDeVenta(
                    detalles
            );

            sincronizarInventarioConLotes(
                    detalles
            );

        } else {

            restaurarInventario(
                    detalles
            );
        }

        venta.setEstado(
                Venta.Estado.ANULADA
        );

        ventaRepository.save(venta);

        ra.addFlashAttribute(
                "success",
                "Venta anulada correctamente. El inventario y los lotes fueron restaurados."
        );

        return "redirect:/view/ventas";
    }


    // =========================================================
    // ELIMINAR VENTA
    // =========================================================

    @PostMapping("/delete/{id}")
    @Transactional
    public String eliminar(
            @PathVariable Long id,
            RedirectAttributes ra,
            Authentication authentication) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para eliminar ventas."
            );

            return "redirect:/view/ventas";
        }

        Venta venta =
                ventaRepository
                        .findById(id)
                        .orElse(null);

        if (venta == null) {

            ra.addFlashAttribute(
                    "error",
                    "Venta no encontrada."
            );

            return "redirect:/view/ventas";
        }

        List<DetalleVenta> detalles =
                detallesDeVenta(id);

        boolean tieneAsignaciones =
                tieneAsignacionesDeLotes(
                        detalles
                );

        if (!inventarioYaLiberado(
                venta.getEstado()
        )) {

            if (tieneAsignaciones) {

                restaurarLotesDeVenta(
                        detalles
                );

                sincronizarInventarioConLotes(
                        detalles
                );

            } else {

                restaurarInventario(
                        detalles
                );
            }
        }

        for (DetalleVenta detalle : detalles) {

            List<DetalleVentaLote> asignaciones =
                    detalleVentaLoteRepository
                            .buscarPorDetalleVenta(
                                    detalle.getId_detalle_venta()
                            );

            if (!asignaciones.isEmpty()) {

                detalleVentaLoteRepository.deleteAll(
                        asignaciones
                );
            }
        }

        detalleVentaRepository.deleteAll(
                detalles
        );

        ventaRepository.delete(venta);

        ra.addFlashAttribute(
                "success",
                "Venta eliminada correctamente."
        );

        return "redirect:/view/ventas";
    }


    // =========================================================
    // VER DETALLES
    // =========================================================

    @GetMapping("/{id}/detalles")
    public String detalles(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra,
            Authentication authentication) {

        Venta venta =
                ventaRepository
                        .findById(id)
                        .orElse(null);

        if (venta == null) {

            ra.addFlashAttribute(
                    "error",
                    "Venta no encontrada."
            );

            return "redirect:/view/ventas";
        }

        List<DetalleVenta> detalles =
                detallesDeVenta(id);

        Map<Long, String> nombresProductos =
                new HashMap<>();

        for (Productos producto :
                productosRepository.findAll()) {

            nombresProductos.put(
                    producto.getId_producto(),
                    producto.getNombre()
            );
        }

        BigDecimal subtotalProductos =
                BigDecimal.ZERO;

        for (DetalleVenta detalle : detalles) {

            if (detalle.getSubtotal() != null) {

                subtotalProductos =
                        subtotalProductos.add(
                                detalle.getSubtotal()
                        );
            }
        }

        model.addAttribute("nombresClientes", cargarMapaClientes());
        model.addAttribute("nombresEmpleados", cargarMapaEmpleados());
        model.addAttribute("nombresMetodosPago", cargarMapaMetodosPago());

        model.addAttribute(
                "venta",
                venta
        );

        model.addAttribute(
                "detalles",
                detalles
        );

        model.addAttribute(
                "nombresProductos",
                nombresProductos
        );

        model.addAttribute(
                "subtotalProductos",
                subtotalProductos
        );

        model.addAttribute(
                "pageTitle",
                "Detalle de Venta #"
                        + venta.getId_venta()
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta de los productos y valores de la venta seleccionada."
        );

        model.addAttribute(
                "activeMenu",
                "ventas"
        );

        model.addAttribute(
                "userName",
                "Administrador"
        );

        model.addAttribute(
                "userRole",
                "Farmacia Central"
        );

        model.addAttribute(
                "puedeModificar",
                puedeModificar(authentication)
        );

        return "Ventas/detalleVentas";
    }


    // =========================================================
    // EL FLUJO DE PRODUCTOS YA NO SE REGISTRA DESDE DETALLES
    // =========================================================

    @GetMapping("/{id}/detalles/form")
    public String detalleForm(
            @PathVariable Long id,
            RedirectAttributes ra) {

        ra.addFlashAttribute(
                "error",
                "Los productos se registran desde Nueva venta."
        );

        return "redirect:/view/ventas/"
                + id
                + "/detalles";
    }


    @GetMapping("/{id}/detalles/edit/{detalleId}")
    public String editarDetalle(
            @PathVariable Long id,
            @PathVariable Long detalleId,
            RedirectAttributes ra) {

        ra.addFlashAttribute(
                "error",
                "Los detalles de una venta registrada no se editan. Use Nueva venta."
        );

        return "redirect:/view/ventas/"
                + id
                + "/detalles";
    }


    @PostMapping("/{id}/detalles/save")
    public String guardarDetalle(
            @PathVariable Long id,
            RedirectAttributes ra) {

        ra.addFlashAttribute(
                "error",
                "Los productos se registran al confirmar una nueva venta."
        );

        return "redirect:/view/ventas/"
                + id
                + "/detalles";
    }


    @PostMapping("/{id}/detalles/delete/{detalleId}")
    public String eliminarDetalle(
            @PathVariable Long id,
            @PathVariable Long detalleId,
            RedirectAttributes ra) {

        ra.addFlashAttribute(
                "error",
                "No es posible quitar productos desde Detalles. Anule la venta si corresponde."
        );

        return "redirect:/view/ventas/"
                + id
                + "/detalles";
    }


    // =========================================================
    // CARGAR DATOS DEL FORMULARIO
    // =========================================================

    private void cargarDatosFormulario(
            Model model,
            Venta venta) {

        Map<Long, BigDecimal> preciosPorProducto =
                new HashMap<>();

        Map<Long, Integer> stockPorProducto =
                new HashMap<>();

        for (Productos producto :
                productosRepository.findAll()) {

            if (producto.getPrecio_venta() != null) {

                preciosPorProducto.put(
                        producto.getId_producto(),
                        producto.getPrecio_venta()
                );
            }
        }

        for (Productos producto :
                productosRepository.findAll()) {

            int stockLotes =
                    obtenerStockDisponibleEnLotes(
                            producto.getId_producto()
                    );

            stockPorProducto.put(
                    producto.getId_producto(),
                    stockLotes
            );
        }

        // =====================================================
        // EMPLEADO ACTUAL
        // =====================================================

        Empleados empleadoActual = null;

        if (venta.getId_empleado() != null) {

            empleadoActual =
                    empleadosRepository
                            .findById(
                                    venta.getId_empleado()
                            )
                            .orElse(null);
        }

        // =====================================================
        // MÉTODOS DE PAGO
        // =====================================================

        List<?> metodosPago =
                metodoPagoRepository.findAll();

        metodosPago.removeIf(metodo -> {

            try {

                String nombre =
                        (String) metodo
                                .getClass()
                                .getMethod("getNombre")
                                .invoke(metodo);

                return nombre != null
                        && nombre.trim()
                        .equalsIgnoreCase("Pago mixto");

            } catch (Exception e) {

                return false;
            }
        });

        model.addAttribute(
                "venta",
                venta
        );

        model.addAttribute(
                "clientes",
                clientesRepository.findAll()
        );

        model.addAttribute(
                "empleados",
                empleadosRepository.findAll()
        );

        model.addAttribute(
                "empleadoActual",
                empleadoActual
        );

        model.addAttribute(
                "metodosPago",
                metodosPago
        );

        model.addAttribute(
                "estados",
                Venta.Estado.values()
        );

        model.addAttribute(
                "productos",
                productosRepository.findAll()
        );

        model.addAttribute(
                "preciosPorProducto",
                preciosPorProducto
        );

        model.addAttribute(
                "stockPorProducto",
                stockPorProducto
        );

        model.addAttribute(
                "activeMenu",
                "ventas"
        );

        model.addAttribute(
                "userName",
                "Administrador"
        );

        model.addAttribute(
                "userRole",
                "Farmacia Central"
        );
    }


    // =========================================================
    // OBTENER EMPLEADO DEL USUARIO AUTENTICADO
    // =========================================================

    private Empleados obtenerEmpleadoAutenticado() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(
                authentication.getName()
        )) {

            throw new IllegalStateException(
                    "No hay un usuario autenticado."
            );
        }

        String username =
                authentication.getName().trim();

        Usuarios usuario =
                usuariosRepository
                        .buscarPorUsername(username)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No se encontró el usuario autenticado."
                                )
                        );

        Empleados empleadoEncontrado = null;

        for (Empleados empleado :
                empleadosRepository.findAll()) {

            if (empleado.getId_usuario() != null
                    && empleado.getId_usuario().equals(
                    usuario.getId_usuario()
            )) {

                empleadoEncontrado = empleado;
                break;
            }
        }

        if (empleadoEncontrado == null) {

            throw new IllegalStateException(
                    "El usuario autenticado no tiene un empleado asociado."
            );
        }

        if (empleadoEncontrado.getEstado()
                == Empleados.EstadoEmpleado.INACTIVO) {

            throw new IllegalStateException(
                    "El empleado autenticado está inactivo."
            );
        }

        return empleadoEncontrado;
    }


    // =========================================================
    // DEVOLVER FORMULARIO CON ERROR
    // =========================================================

    private String devolverFormularioConError(
            Model model,
            Venta venta,
            String error) {

        cargarDatosFormulario(
                model,
                venta
        );

        model.addAttribute(
                "error",
                error
        );

        model.addAttribute(
                "pageTitle",
                "Nueva venta"
        );

        model.addAttribute(
                "pageSubtitle",
                "Registre la venta, agregue los productos y confirme para descontar el inventario."
        );

        return "Ventas/ventasForm";
    }


    // =========================================================
    // CONSTRUIR LÍNEAS
    // =========================================================

    private List<DetalleVenta> construirLineas(
            List<Long> idProductos,
            List<Integer> cantidades) {

        List<DetalleVenta> lineas =
                new ArrayList<>();

        if (idProductos == null
                || cantidades == null) {

            return lineas;
        }

        int total =
                Math.min(
                        idProductos.size(),
                        cantidades.size()
                );

        for (int i = 0; i < total; i++) {

            if (idProductos.get(i) == null) {
                continue;
            }

            DetalleVenta detalle =
                    new DetalleVenta();

            detalle.setId_producto(
                    idProductos.get(i)
            );

            detalle.setCantidad(
                    cantidades.get(i)
            );

            lineas.add(detalle);
        }

        return lineas;
    }


    // =========================================================
    // VALIDAR LÍNEAS
    // =========================================================

    private String validarLineas(
            List<DetalleVenta> lineas) {

        if (lineas.isEmpty()) {

            return "Debe agregar al menos un producto a la venta.";
        }

        for (DetalleVenta detalle : lineas) {

            Productos producto =
                    productosRepository
                            .findById(
                                    detalle.getId_producto()
                            )
                            .orElse(null);

            if (producto == null) {

                return "El producto seleccionado no existe.";
            }

            if (!productoDisponible(producto)) {

                return "El producto "
                        + producto.getNombre()
                        + " no está disponible para la venta.";
            }

            if (detalle.getCantidad() == null
                    || detalle.getCantidad() <= 0) {

                return "La cantidad debe ser mayor que cero.";
            }

            BigDecimal precio =
                    producto.getPrecio_venta();

            if (precio == null
                    || precio.compareTo(
                    BigDecimal.ZERO
            ) <= 0) {

                return "El producto "
                        + producto.getNombre()
                        + " no tiene un precio de venta válido.";
            }

            detalle.setPrecio(precio);

            detalle.setSubtotal(
                    precio.multiply(
                                    BigDecimal.valueOf(
                                            detalle.getCantidad()
                                    )
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
            );
        }

        return null;
    }


    // =========================================================
    // VALIDAR DISPONIBILIDAD REAL
    // =========================================================

    private String validarDisponibilidadCompleta(
            List<DetalleVenta> lineas) {

        Map<Long, Integer> cantidadPorProducto =
                new HashMap<>();

        for (DetalleVenta detalle : lineas) {

            cantidadPorProducto.merge(
                    detalle.getId_producto(),
                    detalle.getCantidad(),
                    Integer::sum
            );
        }

        for (Map.Entry<Long, Integer> entrada :
                cantidadPorProducto.entrySet()) {

            Long idProducto =
                    entrada.getKey();

            int cantidadSolicitada =
                    entrada.getValue();

            int stockLotes =
                    obtenerStockDisponibleEnLotes(
                            idProducto
                    );

            if (stockLotes < cantidadSolicitada) {

                Productos producto =
                        productosRepository
                                .findById(idProducto)
                                .orElse(null);

                String nombre =
                        producto != null
                                ? producto.getNombre()
                                : "seleccionado";

                return "Stock insuficiente para "
                        + nombre
                        + ". Disponible en lotes: "
                        + stockLotes
                        + " unidad(es).";
            }
        }

        return null;
    }


    // =========================================================
    // OBTENER STOCK TOTAL DISPONIBLE EN LOTES
    // =========================================================

    private int obtenerStockDisponibleEnLotes(
            Long idProducto) {

        List<Lote> lotes =
                obtenerLotesDisponibles(
                        idProducto
                );

        int total = 0;

        for (Lote lote : lotes) {

            if (lote.getCantidad() != null
                    && lote.getCantidad() > 0) {

                total += lote.getCantidad();
            }
        }

        return total;
    }


    // =========================================================
    // DESCONTAR LOTES E INVENTARIO
    // =========================================================

    private void descontarLotesEInventario(
            List<DetalleVenta> detalles) {

        for (DetalleVenta detalle : detalles) {

            descontarLotesFEFO(
                    detalle,
                    detalle.getId_producto(),
                    detalle.getCantidad()
            );
        }

        sincronizarInventarioConLotes(
                detalles
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

        LocalDate hoy =
                LocalDate.now();

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

            lote.setCantidad(
                    cantidadDisponible
                            - cantidadAConsumir
            );

            if (lote.getCantidad() == 0) {

                lote.setEstado(
                        Lote.EstadoLote.AGOTADO
                );
            }

            loteRepository.save(lote);

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

        if (pendiente > 0) {

            throw new IllegalStateException(
                    "No hay suficiente cantidad disponible en los lotes para completar la venta."
            );
        }
    }


    // =========================================================
    // RESTAURAR LOTES DE UNA VENTA
    // =========================================================

    private void restaurarLotesDeVenta(
            List<DetalleVenta> detalles) {

        for (DetalleVenta detalle : detalles) {

            List<DetalleVentaLote> asignaciones =
                    detalleVentaLoteRepository
                            .buscarPorDetalleVenta(
                                    detalle.getId_detalle_venta()
                            );

            if (asignaciones.isEmpty()) {
                continue;
            }

            for (DetalleVentaLote asignacion :
                    asignaciones) {

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

                if (lote.getCantidad() > 0) {

                    if (lote.getFecha_vencimiento() != null
                            && lote.getFecha_vencimiento()
                            .isBefore(LocalDate.now())) {

                        lote.setEstado(
                                Lote.EstadoLote.VENCIDO
                        );

                    } else if (
                            lote.getEstado()
                                    != Lote.EstadoLote.RETIRADO
                    ) {

                        lote.setEstado(
                                Lote.EstadoLote.ACTIVO
                        );
                    }
                }

                loteRepository.save(lote);
            }

            detalleVentaLoteRepository.deleteAll(
                    asignaciones
            );
        }
    }


    // =========================================================
    // SINCRONIZAR INVENTARIO CON LOTES
    // =========================================================

    private void sincronizarInventarioConLotes(
            List<DetalleVenta> detalles) {

        Map<Long, Boolean> productos =
                new HashMap<>();

        for (DetalleVenta detalle : detalles) {

            if (detalle.getId_producto() != null) {

                productos.put(
                        detalle.getId_producto(),
                        true
                );
            }
        }

        for (Long idProducto :
                productos.keySet()) {

            int stockLotes =
                    obtenerStockDisponibleEnLotes(
                            idProducto
                    );

            Inventario inventario =
                    inventarioRepository
                            .buscarPorProductoParaActualizar(
                                    idProducto
                            )
                            .orElse(null);

            if (inventario == null) {

                inventario =
                        new Inventario();

                inventario.setId_producto(
                        idProducto
                );

                inventario.setStock_minimo(0);
                inventario.setStock_maximo(0);
            }

            inventario.setStock_actual(
                    stockLotes
            );

            inventarioRepository.save(
                    inventario
            );
        }
    }


    // =========================================================
    // RESTAURAR INVENTARIO
    // =========================================================

    private void restaurarInventario(
            List<DetalleVenta> detalles) {

        for (DetalleVenta detalle : detalles) {

            if (detalle.getId_producto() == null
                    || detalle.getCantidad() == null) {

                continue;
            }

            Inventario inventario =
                    inventarioRepository
                            .buscarPorProductoParaActualizar(
                                    detalle.getId_producto()
                            )
                            .orElse(null);

            if (inventario == null) {
                continue;
            }

            Integer stockActual =
                    inventario.getStock_actual();

            if (stockActual == null) {
                stockActual = 0;
            }

            inventario.setStock_actual(
                    stockActual
                            + detalle.getCantidad()
            );

            inventarioRepository.save(
                    inventario
            );
        }
    }


    // =========================================================
    // COMPROBAR SI EXISTEN ASIGNACIONES DE LOTES
    // =========================================================

    private boolean tieneAsignacionesDeLotes(
            List<DetalleVenta> detalles) {

        for (DetalleVenta detalle : detalles) {

            List<DetalleVentaLote> asignaciones =
                    detalleVentaLoteRepository
                            .buscarPorDetalleVenta(
                                    detalle.getId_detalle_venta()
                            );

            if (!asignaciones.isEmpty()) {

                return true;
            }
        }

        return false;
    }


    // =========================================================
    // RECALCULAR TOTAL
    // =========================================================

    private void recalcularTotal(
            Venta venta) {

        List<DetalleVenta> detalles =
                detallesDeVenta(
                        venta.getId_venta()
                );

        BigDecimal subtotal =
                BigDecimal.ZERO;

        for (DetalleVenta detalle : detalles) {

            if (detalle.getSubtotal() != null) {

                subtotal =
                        subtotal.add(
                                detalle.getSubtotal()
                        );
            }
        }

        BigDecimal descuento =
                venta.getDescuento() != null
                        ? venta.getDescuento()
                        : BigDecimal.ZERO;

        BigDecimal impuestos =
                subtotal
                        .multiply(
                                PORCENTAJE_IMPUESTO
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal total =
                subtotal
                        .subtract(descuento)
                        .add(impuestos);

        if (total.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            total = BigDecimal.ZERO;
        }

        venta.setImpuestos(
                impuestos
        );

        venta.setTotal(
                total
        );

        ventaRepository.save(
                venta
        );
    }


    // =========================================================
    // OBTENER DETALLES DE UNA VENTA
    // =========================================================

    private List<DetalleVenta> detallesDeVenta(
            Long idVenta) {

        return detalleVentaRepository.buscarPorVenta(
                idVenta
        );
    }


    // =========================================================
    // VALIDAR ESTADO DE VENTA
    // =========================================================

    private boolean inventarioYaLiberado(
            Venta.Estado estado) {

        return estado == Venta.Estado.ANULADA
                || estado == Venta.Estado.DEVUELTA;
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
    // PERMISOS DE VENTAS
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
                                || authority.getAuthority()
                                .equals("ROLE_VENDEDOR")
                );
    }
}