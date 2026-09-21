package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Venta;
import com.vitaNova.vitaNova.repository.CompraRepository;
import com.vitaNova.vitaNova.repository.InventarioRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import com.vitaNova.vitaNova.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ReportesView {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private InventarioRepository inventarioRepository;


    // =========================================================
    // PANTALLA PRINCIPAL DE REPORTES
    // =========================================================

    @GetMapping("/view/reportes")
    public String reportes(Model model) {

        var ventas = ventaRepository.findAll();
        var compras = compraRepository.findAll();
        var productos = productosRepository.findAll();
        var inventarios = inventarioRepository.findAll();

        long cantidadVentasPagadas = ventas.stream()
                .filter(venta -> venta.getEstado() == Venta.Estado.PAGADA)
                .count();

        BigDecimal totalVentasPagadas = ventas.stream()
                .filter(venta -> venta.getEstado() == Venta.Estado.PAGADA)
                .map(Venta::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long cantidadCompras = compras.size();

        BigDecimal totalCompras = compras.stream()
                .map(compra -> compra.getTotal())
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long cantidadProductos = productos.size();

        long cantidadInventarios = inventarios.size();

        long productosStockBajo = inventarios.stream()
                .filter(inventario ->
                        inventario.getStock_actual() != null
                                && inventario.getStock_minimo() != null
                                && inventario.getStock_actual() <= inventario.getStock_minimo())
                .count();

        model.addAttribute("pageTitle", "Reportes");

        model.addAttribute(
                "pageSubtitle",
                "Consulte y genere reportes de ventas, compras, productos e inventario."
        );

        model.addAttribute("activeMenu", "reportes");

        model.addAttribute("cantidadVentasPagadas", cantidadVentasPagadas);
        model.addAttribute("totalVentasPagadas", totalVentasPagadas);

        model.addAttribute("cantidadCompras", cantidadCompras);
        model.addAttribute("totalCompras", totalCompras);

        model.addAttribute("cantidadProductos", cantidadProductos);

        model.addAttribute("cantidadInventarios", cantidadInventarios);
        model.addAttribute("productosStockBajo", productosStockBajo);

        return "reportes/Reportes";
    }


    // =========================================================
    // PDF DE VENTAS
    // =========================================================

    @GetMapping("/view/reportes/ventas")
    public String reporteVentas(Model model) {

        var ventasPagadas = ventaRepository.findAll()
                .stream()
                .filter(venta -> venta.getEstado() == Venta.Estado.PAGADA)
                .toList();

        BigDecimal totalVentas = ventasPagadas.stream()
                .map(Venta::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String fechaGeneracion = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        model.addAttribute("ventas", ventasPagadas);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("fechaGeneracion", fechaGeneracion);

        return "reportes/ReporteVentas";
    }


    // =========================================================
    // PDF DE COMPRAS
    // =========================================================

    @GetMapping("/view/reportes/compras")
    public String reporteCompras(Model model) {

        var compras = compraRepository.findAll();

        BigDecimal totalCompras = compras.stream()
                .map(compra -> compra.getTotal())
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String fechaGeneracion = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        model.addAttribute("compras", compras);
        model.addAttribute("totalCompras", totalCompras);
        model.addAttribute("fechaGeneracion", fechaGeneracion);

        return "reportes/ReporteCompras";
    }


    // =========================================================
    // PDF DE PRODUCTOS
    // =========================================================

    @GetMapping("/view/reportes/productos")
    public String reporteProductos(Model model) {

        var productos = productosRepository.findAll();

        String fechaGeneracion = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        model.addAttribute("productos", productos);
        model.addAttribute("fechaGeneracion", fechaGeneracion);

        return "reportes/ReporteProductos";
    }


    // =========================================================
    // PDF DE INVENTARIO
    // =========================================================

    @GetMapping("/view/reportes/inventario")
    public String reporteInventario(Model model) {

        var inventarios = inventarioRepository.findAll();

        long productosStockBajo = inventarios.stream()
                .filter(inventario ->
                        inventario.getStock_actual() != null
                                && inventario.getStock_minimo() != null
                                && inventario.getStock_actual() <= inventario.getStock_minimo())
                .count();

        String fechaGeneracion = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        model.addAttribute("inventarios", inventarios);
        model.addAttribute("productosStockBajo", productosStockBajo);
        model.addAttribute("fechaGeneracion", fechaGeneracion);

        return "reportes/ReporteInventario";
    }


    // =========================================================
    // EXCEL / CSV DE VENTAS
    // =========================================================

    @GetMapping("/view/reportes/ventas/excel")
    public ResponseEntity<byte[]> excelVentas() {

        var ventas = ventaRepository.findAll()
                .stream()
                .filter(venta -> venta.getEstado() == Venta.Estado.PAGADA)
                .toList();

        StringBuilder csv = new StringBuilder();

        csv.append('\uFEFF');
        csv.append("ID;Fecha;Total;Descuento;Impuestos;Estado\n");

        for (Venta venta : ventas) {

            csv.append(valor(venta.getId_venta()))
                    .append(";")
                    .append(valor(venta.getFecha()))
                    .append(";")
                    .append(valor(venta.getTotal()))
                    .append(";")
                    .append(valor(venta.getDescuento()))
                    .append(";")
                    .append(valor(venta.getImpuestos()))
                    .append(";")
                    .append(valor(venta.getEstado()))
                    .append("\n");
        }

        return descargarCsv(csv.toString(), "reporte_ventas.csv");
    }


    // =========================================================
    // EXCEL / CSV DE COMPRAS
    // =========================================================

    @GetMapping("/view/reportes/compras/excel")
    public ResponseEntity<byte[]> excelCompras() {

        var compras = compraRepository.findAll();

        StringBuilder csv = new StringBuilder();

        csv.append('\uFEFF');
        csv.append("ID;Fecha;Total;Estado;ID Proveedor\n");

        for (var compra : compras) {

            csv.append(valor(compra.getId_compra()))
                    .append(";")
                    .append(valor(compra.getFecha()))
                    .append(";")
                    .append(valor(compra.getTotal()))
                    .append(";")
                    .append(valor(compra.getEstado()))
                    .append(";")
                    .append(valor(compra.getId_proveedor()))
                    .append("\n");
        }

        return descargarCsv(csv.toString(), "reporte_compras.csv");
    }


    // =========================================================
    // EXCEL / CSV DE PRODUCTOS
    // =========================================================

    @GetMapping("/view/reportes/productos/excel")
    public ResponseEntity<byte[]> excelProductos() {

        var productos = productosRepository.findAll();

        StringBuilder csv = new StringBuilder();

        csv.append('\uFEFF');

        csv.append(
                "ID;Nombre;Código Interno;Descripción;Precio Compra;" +
                        "Precio Venta;Código Barras;Ubicación;Estado;ID Categoría;ID Proveedor\n"
        );

        for (var producto : productos) {

            csv.append(valor(producto.getId_producto()))
                    .append(";")
                    .append(valor(producto.getNombre()))
                    .append(";")
                    .append(valor(producto.getCodigo_interno()))
                    .append(";")
                    .append(valor(producto.getDescripcion()))
                    .append(";")
                    .append(valor(producto.getPrecio_compra()))
                    .append(";")
                    .append(valor(producto.getPrecio_venta()))
                    .append(";")
                    .append(valor(producto.getCodigo_barras()))
                    .append(";")
                    .append(valor(producto.getUbicacion()))
                    .append(";")
                    .append(valor(producto.getEstado()))
                    .append(";")
                    .append(valor(producto.getId_categoria()))
                    .append(";")
                    .append(valor(producto.getId_proveedor()))
                    .append("\n");
        }

        return descargarCsv(csv.toString(), "reporte_productos.csv");
    }


    // =========================================================
    // EXCEL / CSV DE INVENTARIO
    // =========================================================

    @GetMapping("/view/reportes/inventario/excel")
    public ResponseEntity<byte[]> excelInventario() {

        var inventarios = inventarioRepository.findAll();

        StringBuilder csv = new StringBuilder();

        csv.append('\uFEFF');

        csv.append(
                "ID Inventario;ID Producto;Producto;" +
                        "Stock Actual;Stock Mínimo;Stock Máximo;Estado Stock\n"
        );

        for (var inventario : inventarios) {

            String nombreProducto = "";

            if (inventario.getProducto() != null) {
                nombreProducto = inventario.getProducto().getNombre();
            }

            String estadoStock = "Normal";

            if (inventario.getStock_actual() != null
                    && inventario.getStock_minimo() != null
                    && inventario.getStock_actual() <= inventario.getStock_minimo()) {

                estadoStock = "Stock bajo";
            }

            csv.append(valor(inventario.getId_inventario()))
                    .append(";")
                    .append(valor(inventario.getId_producto()))
                    .append(";")
                    .append(valor(nombreProducto))
                    .append(";")
                    .append(valor(inventario.getStock_actual()))
                    .append(";")
                    .append(valor(inventario.getStock_minimo()))
                    .append(";")
                    .append(valor(inventario.getStock_maximo()))
                    .append(";")
                    .append(valor(estadoStock))
                    .append("\n");
        }

        return descargarCsv(csv.toString(), "reporte_inventario.csv");
    }


    // =========================================================
    // MÉTODOS AUXILIARES PARA CSV
    // =========================================================

    private String valor(Object valor) {

        if (valor == null) {
            return "";
        }

        String texto = String.valueOf(valor);

        texto = texto.replace("\"", "\"\"");

        if (texto.contains(";")
                || texto.contains("\"")
                || texto.contains("\n")
                || texto.contains("\r")) {

            return "\"" + texto + "\"";
        }

        return texto;
    }


    private ResponseEntity<byte[]> descargarCsv(
            String contenido,
            String nombreArchivo) {

        byte[] archivo = contenido.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\""
                )
                .contentType(
                        MediaType.parseMediaType("text/csv; charset=UTF-8")
                )
                .body(archivo);
    }
}
