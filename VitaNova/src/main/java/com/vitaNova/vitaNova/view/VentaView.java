package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.DetalleVenta;
import com.vitaNova.vitaNova.model.Inventario;
import com.vitaNova.vitaNova.model.Productos;
import com.vitaNova.vitaNova.model.Venta;
import com.vitaNova.vitaNova.repository.ClientesRepository;
import com.vitaNova.vitaNova.repository.DetalleVentaRepository;
import com.vitaNova.vitaNova.repository.EmpleadosRepository;
import com.vitaNova.vitaNova.repository.InventarioRepository;
import com.vitaNova.vitaNova.repository.MetodoPagoRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import com.vitaNova.vitaNova.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/view/ventas")
public class VentaView {

    private static final BigDecimal PORCENTAJE_IMPUESTO = new BigDecimal("0.19");

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private ClientesRepository clientesRepository;

    @Autowired
    private EmpleadosRepository empleadosRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;


    // =========================================================
    // LISTAR VENTAS
    // =========================================================

    @GetMapping
    public String listar(Model model) {

        model.addAttribute("ventas", ventaRepository.findAll());
        model.addAttribute("pageTitle", "Gestión de Ventas");
        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de las ventas de la farmacia."
        );
        model.addAttribute("activeMenu", "ventas");
        model.addAttribute("userName", "Administrador");
        model.addAttribute("userRole", "Farmacia Central");

        return "Ventas/ventas";
    }


    // =========================================================
    // FORMULARIO NUEVA VENTA
    // =========================================================

    @GetMapping("/form")
    public String form(Model model) {

        Venta venta = new Venta();

        venta.setFecha(LocalDateTime.now());
        venta.setDescuento(BigDecimal.ZERO);
        venta.setImpuestos(BigDecimal.ZERO);
        venta.setTotal(BigDecimal.ZERO);
        venta.setEstado(Venta.Estado.PAGADA);

        cargarDatosFormulario(model, venta);

        model.addAttribute("pageTitle", "Nueva venta");
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
            @RequestParam(value = "id_producto", required = false) List<Long> idProductos,
            @RequestParam(value = "cantidad", required = false) List<Integer> cantidades,
            Model model,
            RedirectAttributes ra) {

        if (venta.getFecha() == null) {
            venta.setFecha(LocalDateTime.now());
        }

        if (venta.getDescuento() == null) {
            venta.setDescuento(BigDecimal.ZERO);
        }

        if (venta.getEstado() == null) {
            venta.setEstado(Venta.Estado.PAGADA);
        }

        List<DetalleVenta> lineas = construirLineas(idProductos, cantidades);
        String errorLineas = validarLineas(lineas);

        if (errorLineas != null) {
            return devolverFormularioConError(model, venta, errorLineas);
        }

        venta.setImpuestos(BigDecimal.ZERO);
        venta.setTotal(BigDecimal.ZERO);

        Venta nuevaVenta = ventaRepository.save(venta);

        for (DetalleVenta detalle : lineas) {
            detalle.setId_venta(nuevaVenta.getId_venta());
            detalleVentaRepository.save(detalle);
        }

        if (!inventarioYaLiberado(nuevaVenta.getEstado())) {
            String errorInventario = descontarInventario(lineas);

            if (errorInventario != null) {
                throw new IllegalStateException(errorInventario);
            }
        }

        recalcularTotal(nuevaVenta);

        ra.addFlashAttribute(
                "success",
                "Venta registrada correctamente. El inventario fue actualizado."
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
            RedirectAttributes ra) {

        Venta venta = ventaRepository.findById(id).orElse(null);

        if (venta == null) {

            ra.addFlashAttribute(
                    "error",
                    "Venta no encontrada."
            );

            return "redirect:/view/ventas";
        }

        cargarDatosFormulario(model, venta);

        model.addAttribute("pageTitle", "Editar venta");
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
            RedirectAttributes ra) {

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

        Venta.Estado estadoAnterior = venta.getEstado();

        if (datos.getFecha() != null) {
            venta.setFecha(datos.getFecha());
        }

        venta.setId_cliente(datos.getId_cliente());
        venta.setId_empleado(datos.getId_empleado());
        venta.setId_metodo_pago(datos.getId_metodo_pago());

        venta.setDescuento(
                datos.getDescuento() != null
                        ? datos.getDescuento()
                        : BigDecimal.ZERO
        );

        if (datos.getEstado() != null) {
            venta.setEstado(datos.getEstado());
        }

        if (!inventarioYaLiberado(estadoAnterior)
                && inventarioYaLiberado(venta.getEstado())) {

            restaurarInventario(detallesDeVenta(venta.getId_venta()));
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
            RedirectAttributes ra) {

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

        if (inventarioYaLiberado(venta.getEstado())) {

            ra.addFlashAttribute(
                    "error",
                    "La venta ya está anulada o devuelta. El inventario no se modifica otra vez."
            );

            return "redirect:/view/ventas";
        }

        restaurarInventario(detallesDeVenta(id));
        venta.setEstado(Venta.Estado.ANULADA);
        ventaRepository.save(venta);

        ra.addFlashAttribute(
                "success",
                "Venta anulada correctamente. El inventario fue restaurado."
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
            RedirectAttributes ra) {

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

        List<DetalleVenta> detalles = detallesDeVenta(id);

        if (!inventarioYaLiberado(venta.getEstado())) {
            restaurarInventario(detalles);
        }

        detalleVentaRepository.deleteAll(detalles);
        ventaRepository.delete(venta);

        ra.addFlashAttribute(
                "success",
                "Venta eliminada correctamente."
        );

        return "redirect:/view/ventas";
    }


    // =========================================================
    // VER DETALLES (SOLO CONSULTA)
    // =========================================================

    @GetMapping("/{id}/detalles")
    public String detalles(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

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

        List<DetalleVenta> detalles = detallesDeVenta(id);
        Map<Long, String> nombresProductos = new HashMap<>();

        for (Productos producto : productosRepository.findAll()) {
            nombresProductos.put(producto.getId_producto(), producto.getNombre());
        }

        BigDecimal subtotalProductos = BigDecimal.ZERO;

        for (DetalleVenta detalle : detalles) {
            if (detalle.getSubtotal() != null) {
                subtotalProductos = subtotalProductos.add(detalle.getSubtotal());
            }
        }

        model.addAttribute("venta", venta);
        model.addAttribute("detalles", detalles);
        model.addAttribute("nombresProductos", nombresProductos);
        model.addAttribute("subtotalProductos", subtotalProductos);

        model.addAttribute(
                "pageTitle",
                "Detalle de Venta #" + venta.getId_venta()
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta de los productos y valores de la venta seleccionada."
        );

        model.addAttribute("activeMenu", "ventas");
        model.addAttribute("userName", "Administrador");
        model.addAttribute("userRole", "Farmacia Central");

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

        return "redirect:/view/ventas/" + id + "/detalles";
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

        return "redirect:/view/ventas/" + id + "/detalles";
    }

    @PostMapping("/{id}/detalles/save")
    public String guardarDetalle(
            @PathVariable Long id,
            RedirectAttributes ra) {

        ra.addFlashAttribute(
                "error",
                "Los productos se registran al confirmar una nueva venta."
        );

        return "redirect:/view/ventas/" + id + "/detalles";
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

        return "redirect:/view/ventas/" + id + "/detalles";
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private void cargarDatosFormulario(Model model, Venta venta) {

        Map<Long, BigDecimal> preciosPorProducto = new HashMap<>();
        Map<Long, Integer> stockPorProducto = new HashMap<>();

        for (Productos producto : productosRepository.findAll()) {
            if (producto.getPrecio_venta() != null) {
                preciosPorProducto.put(
                        producto.getId_producto(),
                        producto.getPrecio_venta()
                );
            }
        }

        for (Inventario inventario : inventarioRepository.findAll()) {
            stockPorProducto.put(
                    inventario.getId_producto(),
                    inventario.getStock_actual() != null
                            ? inventario.getStock_actual()
                            : 0
            );
        }

        model.addAttribute("venta", venta);
        model.addAttribute("clientes", clientesRepository.findAll());
        model.addAttribute("empleados", empleadosRepository.findAll());
        model.addAttribute("metodosPago", metodoPagoRepository.findAll());
        model.addAttribute("estados", Venta.Estado.values());
        model.addAttribute("productos", productosRepository.findAll());
        model.addAttribute("preciosPorProducto", preciosPorProducto);
        model.addAttribute("stockPorProducto", stockPorProducto);
        model.addAttribute("activeMenu", "ventas");
        model.addAttribute("userName", "Administrador");
        model.addAttribute("userRole", "Farmacia Central");
    }

    private String devolverFormularioConError(
            Model model,
            Venta venta,
            String error) {

        cargarDatosFormulario(model, venta);
        model.addAttribute("error", error);
        model.addAttribute("pageTitle", "Nueva venta");
        model.addAttribute(
                "pageSubtitle",
                "Registre la venta, agregue los productos y confirme para descontar el inventario."
        );

        return "Ventas/ventasForm";
    }

    private List<DetalleVenta> construirLineas(
            List<Long> idProductos,
            List<Integer> cantidades) {

        List<DetalleVenta> lineas = new ArrayList<>();

        if (idProductos == null || cantidades == null) {
            return lineas;
        }

        int total = Math.min(idProductos.size(), cantidades.size());

        for (int i = 0; i < total; i++) {

            if (idProductos.get(i) == null) {
                continue;
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setId_producto(idProductos.get(i));
            detalle.setCantidad(cantidades.get(i));
            lineas.add(detalle);
        }

        return lineas;
    }

    private String validarLineas(List<DetalleVenta> lineas) {

        if (lineas.isEmpty()) {
            return "Debe agregar al menos un producto a la venta.";
        }

        Map<Long, Integer> cantidadPorProducto = new HashMap<>();

        for (DetalleVenta detalle : lineas) {

            Productos producto =
                    productosRepository
                            .findById(detalle.getId_producto())
                            .orElse(null);

            if (producto == null) {
                return "El producto seleccionado no existe.";
            }

            if (!productoDisponible(producto)) {
                return "El producto "
                        + producto.getNombre()
                        + " no está disponible para la venta.";
            }

            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                return "La cantidad debe ser mayor que cero.";
            }

            BigDecimal precio = producto.getPrecio_venta();

            if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
                return "El producto "
                        + producto.getNombre()
                        + " no tiene un precio de venta válido.";
            }

            detalle.setPrecio(precio);
            detalle.setSubtotal(
                    precio.multiply(BigDecimal.valueOf(detalle.getCantidad()))
                            .setScale(2, RoundingMode.HALF_UP)
            );

            cantidadPorProducto.merge(
                    detalle.getId_producto(),
                    detalle.getCantidad(),
                    Integer::sum
            );
        }

        for (Map.Entry<Long, Integer> entrada : cantidadPorProducto.entrySet()) {

            Inventario inventario =
                    inventarioRepository
                            .buscarPorProductoParaActualizar(entrada.getKey())
                            .orElse(null);

            if (inventario == null || inventario.getStock_actual() == null) {
                return "El producto no tiene un registro de inventario.";
            }

            if (entrada.getValue() > inventario.getStock_actual()) {
                String nombre = "seleccionado";
                Productos producto =
                        productosRepository.findById(entrada.getKey()).orElse(null);

                if (producto != null) {
                    nombre = producto.getNombre();
                }

                return "Stock insuficiente para "
                        + nombre
                        + ". Disponible: "
                        + inventario.getStock_actual()
                        + " unidad(es).";
            }
        }

        return null;
    }

    private String descontarInventario(List<DetalleVenta> lineas) {

        Map<Long, Integer> cantidadPorProducto = new HashMap<>();

        for (DetalleVenta detalle : lineas) {
            cantidadPorProducto.merge(
                    detalle.getId_producto(),
                    detalle.getCantidad(),
                    Integer::sum
            );
        }

        for (Map.Entry<Long, Integer> entrada : cantidadPorProducto.entrySet()) {

            Inventario inventario =
                    inventarioRepository
                            .buscarPorProductoParaActualizar(entrada.getKey())
                            .orElse(null);

            if (inventario == null || inventario.getStock_actual() == null) {
                return "El producto no tiene un registro de inventario.";
            }

            int nuevoStock = inventario.getStock_actual() - entrada.getValue();

            if (nuevoStock < 0) {
                return "Stock insuficiente. La venta no se registró.";
            }

            inventario.setStock_actual(nuevoStock);
            inventarioRepository.save(inventario);
        }

        return null;
    }

    private void restaurarInventario(List<DetalleVenta> detalles) {

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

            Integer stockActual = inventario.getStock_actual();

            if (stockActual == null) {
                stockActual = 0;
            }

            inventario.setStock_actual(stockActual + detalle.getCantidad());
            inventarioRepository.save(inventario);
        }
    }

    private List<DetalleVenta> detallesDeVenta(Long idVenta) {
        return detalleVentaRepository.buscarPorVenta(idVenta);
    }

    private boolean inventarioYaLiberado(Venta.Estado estado) {
        return estado == Venta.Estado.ANULADA
                || estado == Venta.Estado.DEVUELTA;
    }

    private boolean productoDisponible(Productos producto) {

        if (producto.getEstado() == null) {
            return true;
        }

        String estado = producto.getEstado().trim();

        return !"INACTIVO".equalsIgnoreCase(estado)
                && !"AGOTADO".equalsIgnoreCase(estado);
    }

    private void recalcularTotal(Venta venta) {

        List<DetalleVenta> detalles = detallesDeVenta(venta.getId_venta());

        BigDecimal subtotal = BigDecimal.ZERO;

        for (DetalleVenta detalle : detalles) {
            if (detalle.getSubtotal() != null) {
                subtotal = subtotal.add(detalle.getSubtotal());
            }
        }

        BigDecimal descuento =
                venta.getDescuento() != null
                        ? venta.getDescuento()
                        : BigDecimal.ZERO;

        BigDecimal impuestos =
                subtotal.multiply(PORCENTAJE_IMPUESTO)
                        .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.subtract(descuento).add(impuestos);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        venta.setImpuestos(impuestos);
        venta.setTotal(total);
        ventaRepository.save(venta);
    }
}
