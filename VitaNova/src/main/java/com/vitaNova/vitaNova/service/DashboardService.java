package com.vitaNova.vitaNova.service;

import com.vitaNova.vitaNova.dto.DashboardAlerta;
import com.vitaNova.vitaNova.dto.DashboardProductoTop;
import com.vitaNova.vitaNova.dto.DashboardResumen;
import com.vitaNova.vitaNova.dto.DashboardVentaMensual;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final String[] MONTH_LABELS = {
            "ENE", "FEB", "MAR", "ABR", "MAY", "JUN",
            "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"
    };

    private static final double CHART_TOP = 24d;
    private static final double CHART_BASELINE = 170d;
    private static final int CHART_WIDTH = 1000;

    private final JdbcTemplate jdbcTemplate;

    // =========================================================
    // RESUMEN PRINCIPAL
    // =========================================================

    public DashboardResumen obtenerResumen() {

        LocalDate hoy = LocalDate.now();

        long totalProductos = queryLong(
                "SELECT COUNT(*) FROM producto"
        );

        long clientesRegistrados = queryLong(
                "SELECT COUNT(*) FROM cliente"
        );

        long stockBajo = queryLong(
                "SELECT COUNT(*) " +
                        "FROM inventario " +
                        "WHERE stock_actual <= stock_minimo"
        );

        long proveedoresRegistrados = queryLong(
                "SELECT COUNT(*) FROM proveedor"
        );

        long empleadosRegistrados = queryLong(
                "SELECT COUNT(*) FROM empleado"
        );

        long comprasPendientes = queryLong(
                "SELECT COUNT(*) " +
                        "FROM compra " +
                        "WHERE estado = 'PENDIENTE'"
        );

        long lotesPorVencer = queryLong(
                "SELECT COUNT(*) " +
                        "FROM lote " +
                        "WHERE fecha_vencimiento BETWEEN ? AND ?",
                Date.valueOf(hoy),
                Date.valueOf(hoy.plusDays(15))
        );

        BigDecimal ventasHoy = queryBigDecimal(
                "SELECT COALESCE(SUM(total), 0) " +
                        "FROM venta " +
                        "WHERE estado = 'PAGADA' " +
                        "AND DATE(fecha) = ?",
                Date.valueOf(hoy)
        );

        // =====================================================
        // VENTAS MENSUALES
        // =====================================================

        List<DashboardVentaMensual> ventasMensuales =
                cargarVentasMensuales(hoy);

        BigDecimal ventasMesActual = BigDecimal.ZERO;

        if (!ventasMensuales.isEmpty()) {

            DashboardVentaMensual ultimoMes =
                    ventasMensuales.get(
                            ventasMensuales.size() - 1
                    );

            if (ultimoMes.getTotal() != null) {
                ventasMesActual = ultimoMes.getTotal();
            }
        }

        // =====================================================
        // PRODUCTOS MÁS VENDIDOS
        // =====================================================

        List<DashboardProductoTop> topProductos =
                cargarTopProductos();

        // =====================================================
        // ALERTAS
        // =====================================================

        List<DashboardAlerta> alertas =
                construirAlertas(
                        lotesPorVencer,
                        stockBajo,
                        comprasPendientes
                );

        // =====================================================
        // GRÁFICO
        // =====================================================

        ChartPoints chartPoints =
                construirChartPoints(ventasMensuales);

        // =====================================================
        // COBERTURA INVENTARIO
        // =====================================================

        long totalInventario =
                queryLong(
                        "SELECT COUNT(*) FROM inventario"
                );

        int coberturaInventarioPorcentaje = 0;

        if (totalInventario > 0) {

            coberturaInventarioPorcentaje =
                    (int) Math.round(
                            ((double) (totalInventario - stockBajo) * 100.0)
                                    / totalInventario
                    );

            coberturaInventarioPorcentaje =
                    Math.max(
                            0,
                            Math.min(
                                    100,
                                    coberturaInventarioPorcentaje
                            )
                    );
        }

        String coberturaInventarioMensaje;

        if (totalInventario == 0) {

            coberturaInventarioMensaje =
                    "Aún no hay productos cargados en inventario.";

        } else {

            coberturaInventarioMensaje =
                    "Cobertura actual del inventario: "
                            + coberturaInventarioPorcentaje
                            + "% de los SKU mantienen stock suficiente.";
        }

        // =====================================================
        // CONSTRUIR RESPUESTA
        // =====================================================

        DashboardResumen resumen =
                new DashboardResumen();

        resumen.setTotalProductosTexto(
                formatCount(totalProductos)
        );

        resumen.setVentasHoyTexto(
                formatMoney(ventasHoy)
        );

        resumen.setStockBajoTexto(
                formatCount(stockBajo)
        );

        resumen.setClientesRegistradosTexto(
                formatCount(clientesRegistrados)
        );

        resumen.setProveedoresRegistradosTexto(
                formatCount(proveedoresRegistrados)
        );

        resumen.setEmpleadosRegistradosTexto(
                formatCount(empleadosRegistrados)
        );

        /*
         * Estos módulos todavía no están implementados:
         *
         * - Facturas
         * - Devoluciones
         * - Reportes
         *
         * Se mantienen en cero para no consultar tablas
         * que todavía no existen.
         */

        resumen.setFacturasEmitidasTexto(
                formatCount(0)
        );

        resumen.setDevolucionesHoyTexto(
                formatCount(0)
        );

        resumen.setComprasPendientesTexto(
                formatCount(comprasPendientes)
        );

        resumen.setLotesPorVencerTexto(
                formatCount(lotesPorVencer)
        );

        resumen.setVentasMensualesTexto(
                formatMoney(ventasMesActual)
        );

        resumen.setCoberturaInventarioPorcentaje(
                coberturaInventarioPorcentaje
        );

        resumen.setCoberturaInventarioMensaje(
                coberturaInventarioMensaje
        );

        resumen.setVentasMensuales(
                ventasMensuales
        );

        resumen.setTopProductos(
                topProductos
        );

        resumen.setAlertas(
                alertas
        );

        resumen.setChartLinePoints(
                chartPoints.linePoints()
        );

        resumen.setChartAreaPoints(
                chartPoints.areaPoints()
        );

        return resumen;
    }

    // =========================================================
    // VENTAS MENSUALES
    // =========================================================

    private List<DashboardVentaMensual> cargarVentasMensuales(
            LocalDate hoy
    ) {

        YearMonth inicio =
                YearMonth.from(hoy).minusMonths(11);

        Map<YearMonth, BigDecimal> totales =
                new HashMap<>();

        try {

            String sql =
                    "SELECT " +
                            "YEAR(fecha) AS anio, " +
                            "MONTH(fecha) AS mes, " +
                            "COALESCE(SUM(total), 0) AS total " +
                            "FROM venta " +
                            "WHERE estado = 'PAGADA' " +
                            "AND fecha >= ? " +
                            "GROUP BY YEAR(fecha), MONTH(fecha) " +
                            "ORDER BY YEAR(fecha), MONTH(fecha)";

            jdbcTemplate.query(
                    sql,
                    ps -> ps.setDate(
                            1,
                            Date.valueOf(
                                    inicio.atDay(1)
                            )
                    ),
                    rs -> {

                        while (rs.next()) {

                            YearMonth key =
                                    YearMonth.of(
                                            rs.getInt("anio"),
                                            rs.getInt("mes")
                                    );

                            BigDecimal total =
                                    rs.getBigDecimal("total");

                            if (total == null) {
                                total = BigDecimal.ZERO;
                            }

                            totales.put(
                                    key,
                                    total
                            );
                        }

                        return null;
                    }
            );

        } catch (DataAccessException ex) {

            totales.clear();
        }

        List<DashboardVentaMensual> resultado =
                new ArrayList<>(12);

        BigDecimal maximo =
                BigDecimal.ZERO;

        for (int i = 0; i < 12; i++) {

            YearMonth periodo =
                    inicio.plusMonths(i);

            BigDecimal total =
                    totales.getOrDefault(
                            periodo,
                            BigDecimal.ZERO
                    );

            DashboardVentaMensual mes =
                    new DashboardVentaMensual();

            mes.setAnio(
                    periodo.getYear()
            );

            mes.setMes(
                    MONTH_LABELS[
                            periodo.getMonthValue() - 1
                            ]
            );

            mes.setTotal(total);

            mes.setTotalTexto(
                    formatMoney(total)
            );

            resultado.add(mes);

            if (total.compareTo(maximo) > 0) {
                maximo = total;
            }
        }

        for (DashboardVentaMensual mes : resultado) {

            int porcentaje = 0;

            if (maximo.compareTo(BigDecimal.ZERO) > 0) {

                double ratio =
                        mes.getTotal().doubleValue()
                                / maximo.doubleValue();

                porcentaje =
                        (int) Math.round(
                                ratio * 100.0
                        );
            }

            mes.setPorcentaje(
                    Math.max(
                            0,
                            Math.min(
                                    100,
                                    porcentaje
                            )
                    )
            );
        }

        return resultado;
    }

    // =========================================================
    // PRODUCTOS MÁS VENDIDOS
    // =========================================================

    private List<DashboardProductoTop> cargarTopProductos() {

        String sql =
                "SELECT " +
                        "p.id_producto, " +
                        "p.nombre, " +
                        "COALESCE(c.nombre, 'Sin categoría') AS categoria, " +
                        "COALESCE(" +
                        "SUM(" +
                        "CASE " +
                        "WHEN v.estado = 'PAGADA' " +
                        "THEN dv.cantidad " +
                        "ELSE 0 " +
                        "END" +
                        "), 0" +
                        ") AS cantidad_vendida, " +
                        "COALESCE(i.stock_actual, 0) AS stock_actual, " +
                        "COALESCE(i.stock_minimo, 0) AS stock_minimo, " +
                        "p.estado AS estado_producto " +
                        "FROM producto p " +
                        "LEFT JOIN categoria c " +
                        "ON c.id_categoria = p.id_categoria " +
                        "LEFT JOIN inventario i " +
                        "ON i.id_producto = p.id_producto " +
                        "LEFT JOIN detalle_venta dv " +
                        "ON dv.id_producto = p.id_producto " +
                        "LEFT JOIN venta v " +
                        "ON v.id_venta = dv.id_venta " +
                        "GROUP BY " +
                        "p.id_producto, " +
                        "p.nombre, " +
                        "c.nombre, " +
                        "i.stock_actual, " +
                        "i.stock_minimo, " +
                        "p.estado " +
                        "ORDER BY cantidad_vendida DESC, p.nombre ASC " +
                        "LIMIT 5";

        try {

            return jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> {

                        DashboardProductoTop producto =
                                new DashboardProductoTop();

                        producto.setIdProducto(
                                rs.getLong(
                                        "id_producto"
                                )
                        );

                        producto.setNombre(
                                rs.getString(
                                        "nombre"
                                )
                        );

                        producto.setCategoria(
                                rs.getString(
                                        "categoria"
                                )
                        );

                        long cantidadVendida =
                                rs.getLong(
                                        "cantidad_vendida"
                                );

                        producto.setCantidadVendida(
                                cantidadVendida
                        );

                        producto.setCantidadVendidaTexto(
                                formatCount(
                                        cantidadVendida
                                )
                        );

                        int stockActual =
                                rs.getInt(
                                        "stock_actual"
                                );

                        int stockMinimo =
                                rs.getInt(
                                        "stock_minimo"
                                );

                        producto.setStockActual(
                                stockActual
                        );

                        producto.setStockMinimo(
                                stockMinimo
                        );

                        String estadoProducto =
                                rs.getString(
                                        "estado_producto"
                                );

                        producto.setEstado(
                                describirEstado(
                                        estadoProducto,
                                        stockActual,
                                        stockMinimo
                                )
                        );

                        producto.setEstadoClase(
                                claseEstado(
                                        estadoProducto,
                                        stockActual,
                                        stockMinimo
                                )
                        );

                        return producto;
                    }
            );

        } catch (DataAccessException ex) {

            return Collections.emptyList();
        }
    }

    // =========================================================
    // ALERTAS
    // =========================================================

    private List<DashboardAlerta> construirAlertas(
            long lotesPorVencer,
            long stockBajo,
            long comprasPendientes
    ) {

        List<DashboardAlerta> alertas =
                new ArrayList<>(3);

        alertas.add(
                crearAlerta(
                        lotesPorVencer > 0
                                ? "danger"
                                : "success",

                        "fa-regular fa-calendar-xmark",

                        lotesPorVencer > 0
                                ? "Próximos a vencer"
                                : "Sin lotes críticos",

                        lotesPorVencer > 0
                                ? lotesPorVencer
                                  + " lotes vencen en menos de 15 días."
                                : "No hay lotes próximos a vencer en los próximos 15 días.",

                        lotesPorVencer > 0
                                ? "Revisar lotes"
                                : "Ver inventario"
                )
        );

        alertas.add(
                crearAlerta(
                        stockBajo > 0
                                ? "warning"
                                : "success",

                        "fa-solid fa-sliders",

                        stockBajo > 0
                                ? "Bajo stock"
                                : "Stock estable",

                        stockBajo > 0
                                ? stockBajo
                                  + " productos alcanzaron el mínimo de existencias."
                                : "No hay productos por debajo del stock mínimo.",

                        "Revisar inventario"
                )
        );

        alertas.add(
                crearAlerta(
                        comprasPendientes > 0
                                ? "warning"
                                : "success",

                        "fa-solid fa-truck",

                        comprasPendientes > 0
                                ? "Compras pendientes"
                                : "Compras al día",

                        comprasPendientes > 0
                                ? comprasPendientes
                                  + " compras están pendientes."
                                : "No hay compras pendientes.",

                        "Ver compras"
                )
        );

        return alertas;
    }

    private DashboardAlerta crearAlerta(
            String severity,
            String iconClass,
            String title,
            String description,
            String actionText
    ) {

        DashboardAlerta alerta =
                new DashboardAlerta();

        alerta.setSeverity(
                severity
        );

        alerta.setIconClass(
                iconClass
        );

        alerta.setTitle(
                title
        );

        alerta.setDescription(
                description
        );

        alerta.setActionText(
                actionText
        );

        alerta.setActionHref(
                "#"
        );

        return alerta;
    }

    // =========================================================
    // GRÁFICO
    // =========================================================

    private ChartPoints construirChartPoints(
            List<DashboardVentaMensual> meses
    ) {

        if (meses == null || meses.isEmpty()) {

            return new ChartPoints(
                    "0," + CHART_BASELINE
                            + " "
                            + CHART_WIDTH
                            + ","
                            + CHART_BASELINE,

                    "0," + CHART_BASELINE
                            + " "
                            + CHART_WIDTH
                            + ","
                            + CHART_BASELINE
                            + " "
                            + CHART_WIDTH
                            + ","
                            + CHART_BASELINE
                            + " 0,"
                            + CHART_BASELINE
            );
        }

        BigDecimal maximo =
                meses.stream()
                        .map(
                                DashboardVentaMensual::getTotal
                        )
                        .filter(
                                Objects::nonNull
                        )
                        .max(
                                BigDecimal::compareTo
                        )
                        .orElse(
                                BigDecimal.ZERO
                        );

        double stepX =
                meses.size() == 1
                        ? CHART_WIDTH
                        : (double) CHART_WIDTH
                          / (meses.size() - 1);

        StringBuilder linePoints =
                new StringBuilder();

        for (int i = 0;
             i < meses.size();
             i++) {

            DashboardVentaMensual mes =
                    meses.get(i);

            double x =
                    stepX * i;

            double y =
                    CHART_BASELINE;

            if (
                    maximo.compareTo(BigDecimal.ZERO) > 0
                            && mes.getTotal() != null
            ) {

                double ratio =
                        mes.getTotal().doubleValue()
                                / maximo.doubleValue();

                y =
                        CHART_BASELINE
                                - (
                                (CHART_BASELINE - CHART_TOP)
                                        * ratio
                        );
            }

            if (i > 0) {
                linePoints.append(' ');
            }

            linePoints.append(
                    String.format(
                            Locale.US,
                            "%.1f,%.1f",
                            x,
                            y
                    )
            );
        }

        String areaPoints =
                linePoints.toString()
                        + " "
                        + CHART_WIDTH
                        + ","
                        + CHART_BASELINE
                        + " 0,"
                        + CHART_BASELINE;

        return new ChartPoints(
                linePoints.toString(),
                areaPoints
        );
    }

    // =========================================================
    // CONSULTA LONG
    // =========================================================

    private long queryLong(
            String sql,
            Object... args
    ) {

        try {

            Long value =
                    jdbcTemplate.queryForObject(
                            sql,
                            Long.class,
                            args
                    );

            if (value == null) {
                return 0L;
            }

            return value;

        } catch (DataAccessException ex) {

            return 0L;
        }
    }

    // =========================================================
    // CONSULTA DECIMAL
    // =========================================================

    private BigDecimal queryBigDecimal(
            String sql,
            Object... args
    ) {

        try {

            BigDecimal value =
                    jdbcTemplate.queryForObject(
                            sql,
                            BigDecimal.class,
                            args
                    );

            if (value == null) {
                return BigDecimal.ZERO;
            }

            return value;

        } catch (DataAccessException ex) {

            return BigDecimal.ZERO;
        }
    }

    // =========================================================
    // FORMATO DE CANTIDADES
    // =========================================================

    private String formatCount(
            long value
    ) {

        return String.format(
                Locale.US,
                "%,d",
                value
        );
    }

    // =========================================================
    // FORMATO DE DINERO
    // =========================================================

    private String formatMoney(
            BigDecimal value
    ) {

        BigDecimal normalized =
                value == null
                        ? BigDecimal.ZERO
                        : value.setScale(
                        0,
                        RoundingMode.HALF_UP
                );

        return "$"
                + String.format(
                Locale.US,
                "%,d",
                normalized.longValue()
        );
    }

    // =========================================================
    // DESCRIPCIÓN DEL ESTADO
    // =========================================================

    private String describirEstado(
            String estadoProducto,
            int stockActual,
            int stockMinimo
    ) {

        if (
                "INACTIVO".equalsIgnoreCase(
                        estadoProducto
                )
        ) {

            return "Inactivo";
        }

        if (stockActual <= 0) {

            return "Sin existencias";
        }

        if (stockActual <= stockMinimo) {

            return "Stock bajo";
        }

        return "Disponible";
    }

    // =========================================================
    // CLASE CSS DEL ESTADO
    // =========================================================

    private String claseEstado(
            String estadoProducto,
            int stockActual,
            int stockMinimo
    ) {

        if (
                "INACTIVO".equalsIgnoreCase(
                        estadoProducto
                )
        ) {

            return "inactive";
        }

        if (stockActual <= 0) {

            return "danger";
        }

        if (stockActual <= stockMinimo) {

            return "warning";
        }

        return "success";
    }

    // =========================================================
    // PUNTOS DEL GRÁFICO
    // =========================================================

    private record ChartPoints(
            String linePoints,
            String areaPoints
    ) {
    }
}
