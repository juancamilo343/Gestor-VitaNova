package com.vitaNova.vitaNova.service;

import com.vitaNova.vitaNova.dto.DashboardAlerta;
import com.vitaNova.vitaNova.dto.DashboardProductoTop;
import com.vitaNova.vitaNova.dto.DashboardResumen;
import com.vitaNova.vitaNova.dto.DashboardVentaMensual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private DashboardService dashboardService;

    /** Valores devueltos por cada COUNT/SUM, indexados por un fragmento del SQL. */
    private final Map<String, Object> scalarResults = new HashMap<>();

    /** Filas simuladas de la consulta de ventas mensuales. */
    private final List<Object[]> ventasRows = new ArrayList<>();

    /** Filas simuladas de la consulta de productos top. */
    private final List<Map<String, Object>> productoRows = new ArrayList<>();

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(jdbcTemplate);
        stubScalarQueries();
        stubVentasMensuales();
        stubTopProductos();
    }

    @Test
    void obtenerResumenFormateaLosContadoresConSeparadorDeMiles() {
        scalarResults.put("FROM producto", 1234L);
        scalarResults.put("FROM cliente", 42L);
        scalarResults.put("FROM factura", 7L);

        DashboardResumen resumen = dashboardService.obtenerResumen();

        assertThat(resumen.getTotalProductosTexto()).isEqualTo("1,234");
        assertThat(resumen.getClientesRegistradosTexto()).isEqualTo("42");
        assertThat(resumen.getFacturasEmitidasTexto()).isEqualTo("7");
    }

    @Test
    void obtenerResumenFormateaLosMontosRedondeandoAPesos() {
        scalarResults.put("FROM venta WHERE estado", new BigDecimal("1234567.89"));

        DashboardResumen resumen = dashboardService.obtenerResumen();

        assertThat(resumen.getVentasHoyTexto()).isEqualTo("$1,234,568");
    }

    @Test
    void obtenerResumenCalculaLaCoberturaDeInventario() {
        scalarResults.put("FROM inventario WHERE stock_actual", 3L);
        scalarResults.put("FROM inventario", 12L);

        DashboardResumen resumen = dashboardService.obtenerResumen();

        assertThat(resumen.getCoberturaInventarioPorcentaje()).isEqualTo(75);
        assertThat(resumen.getCoberturaInventarioMensaje())
                .isEqualTo("Cobertura actual del inventario: 75% de los SKU mantienen stock suficiente.");
    }

    @Test
    void obtenerResumenInformaInventarioVacioCuandoNoHayRegistros() {
        DashboardResumen resumen = dashboardService.obtenerResumen();

        assertThat(resumen.getCoberturaInventarioPorcentaje()).isZero();
        assertThat(resumen.getCoberturaInventarioMensaje())
                .isEqualTo("Aún no hay productos cargados en inventario.");
    }

    @Test
    void obtenerResumenDevuelveDoceMesesConElUltimoComoMesActual() {
        YearMonth mesActual = YearMonth.from(LocalDate.now());
        ventasRows.add(new Object[]{mesActual.getYear(), mesActual.getMonthValue(), new BigDecimal("500")});

        DashboardResumen resumen = dashboardService.obtenerResumen();
        List<DashboardVentaMensual> meses = resumen.getVentasMensuales();

        assertThat(meses).hasSize(12);
        assertThat(meses.get(11).getAnio()).isEqualTo(mesActual.getYear());
        assertThat(meses.get(11).getTotal()).isEqualByComparingTo("500");
        assertThat(meses.get(11).getPorcentaje()).isEqualTo(100);
        assertThat(meses.get(0).getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(meses.get(0).getPorcentaje()).isZero();
        assertThat(resumen.getVentasMensualesTexto()).isEqualTo("$500");
    }

    @Test
    void obtenerResumenConstruyeLosPuntosDelGraficoEntreLaBaseYElTope() {
        YearMonth mesActual = YearMonth.from(LocalDate.now());
        ventasRows.add(new Object[]{mesActual.getYear(), mesActual.getMonthValue(), new BigDecimal("500")});

        DashboardResumen resumen = dashboardService.obtenerResumen();

        assertThat(resumen.getChartLinePoints())
                .startsWith("0.0,170.0")
                .endsWith("1000.0,24.0");
        assertThat(resumen.getChartAreaPoints())
                .startsWith(resumen.getChartLinePoints())
                .endsWith(" 1000,170.0 0,170.0");
    }

    @Test
    void obtenerResumenDejaLaLineaEnLaBaseCuandoNoHayVentas() {
        DashboardResumen resumen = dashboardService.obtenerResumen();

        assertThat(resumen.getChartLinePoints()).startsWith("0.0,170.0").endsWith("1000.0,170.0");
        assertThat(resumen.getVentasMensualesTexto()).isEqualTo("$0");
    }

    @Test
    void obtenerResumenGeneraAlertasCriticasCuandoHayIndicadoresEnRojo() {
        scalarResults.put("FROM lote WHERE", 4L);
        scalarResults.put("FROM inventario WHERE stock_actual", 2L);
        scalarResults.put("FROM compra WHERE", 6L);

        List<DashboardAlerta> alertas = dashboardService.obtenerResumen().getAlertas();

        assertThat(alertas).hasSize(3);
        assertThat(alertas.get(0).getSeverity()).isEqualTo("danger");
        assertThat(alertas.get(0).getTitle()).isEqualTo("Próximos a vencer");
        assertThat(alertas.get(0).getDescription()).isEqualTo("4 lotes vencen en menos de 15 días.");
        assertThat(alertas.get(1).getSeverity()).isEqualTo("warning");
        assertThat(alertas.get(1).getActionText()).isEqualTo("Generar orden de compra");
        assertThat(alertas.get(2).getTitle()).isEqualTo("Compras pendientes");
        assertThat(alertas).allSatisfy(alerta -> assertThat(alerta.getActionHref()).isEqualTo("#"));
    }

    @Test
    void obtenerResumenGeneraAlertasEnVerdeCuandoTodoEstaAlDia() {
        List<DashboardAlerta> alertas = dashboardService.obtenerResumen().getAlertas();

        assertThat(alertas).extracting(DashboardAlerta::getSeverity)
                .containsExactly("success", "success", "success");
        assertThat(alertas).extracting(DashboardAlerta::getTitle)
                .containsExactly("Sin lotes críticos", "Stock estable", "Compras al día");
    }

    @Test
    void obtenerResumenClasificaElEstadoDeLosProductosTop() {
        productoRows.add(productoRow(1L, "Acetaminofén", "Analgésicos", 30L, 50, 10, "ACTIVO"));
        productoRows.add(productoRow(2L, "Ibuprofeno", "Analgésicos", 20L, 5, 10, "ACTIVO"));
        productoRows.add(productoRow(3L, "Loratadina", "Antialérgicos", 10L, 0, 10, "ACTIVO"));
        productoRows.add(productoRow(4L, "Aspirina", "Analgésicos", 5L, 80, 10, "AGOTADO"));
        productoRows.add(productoRow(5L, "Omeprazol", "Digestivos", 1000L, 80, 10, "INACTIVO"));

        List<DashboardProductoTop> top = dashboardService.obtenerResumen().getTopProductos();

        assertThat(top).extracting(DashboardProductoTop::getEstado)
                .containsExactly("Disponible", "Stock bajo", "Agotado", "Agotado", "Inactivo");
        assertThat(top).extracting(DashboardProductoTop::getEstadoClase)
                .containsExactly("success", "warning", "danger", "danger", "danger");
        assertThat(top.get(4).getCantidadVendidaTexto()).isEqualTo("1,000");
        assertThat(top.get(0).getCategoria()).isEqualTo("Analgésicos");
    }

    @Test
    void obtenerResumenUsaValoresNeutrosCuandoLaBaseDeDatosFalla() {
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), any(Object[].class)))
                .thenThrow(new DataAccessResourceFailureException("sin conexión"));
        when(jdbcTemplate.query(anyString(), any(PreparedStatementSetter.class), any(ResultSetExtractor.class)))
                .thenThrow(new DataAccessResourceFailureException("sin conexión"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenThrow(new DataAccessResourceFailureException("sin conexión"));

        DashboardResumen resumen = dashboardService.obtenerResumen();

        assertThat(resumen.getTotalProductosTexto()).isEqualTo("0");
        assertThat(resumen.getVentasHoyTexto()).isEqualTo("$0");
        assertThat(resumen.getTopProductos()).isEmpty();
        assertThat(resumen.getVentasMensuales()).hasSize(12);
        assertThat(resumen.getCoberturaInventarioPorcentaje()).isZero();
        assertThat(resumen.getAlertas()).hasSize(3);
    }

    private void stubScalarQueries() {
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), any(Object[].class)))
                .thenAnswer(invocation -> {
                    String sql = invocation.getArgument(0);
                    Class<?> tipo = invocation.getArgument(1);
                    Object valor = scalarResults.entrySet().stream()
                            .filter(entry -> sql.contains(entry.getKey()))
                            .max(Comparator.comparingInt(entry -> entry.getKey().length()))
                            .map(Map.Entry::getValue)
                            .orElse(null);
                    if (valor != null) {
                        return valor;
                    }
                    return tipo == BigDecimal.class ? BigDecimal.ZERO : 0L;
                });
    }

    private void stubVentasMensuales() {
        when(jdbcTemplate.query(anyString(), any(PreparedStatementSetter.class), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    PreparedStatementSetter setter = invocation.getArgument(1);
                    setter.setValues(mock(PreparedStatement.class));
                    ResultSetExtractor<?> extractor = invocation.getArgument(2);
                    return extractor.extractData(ventasResultSet());
                });
    }

    private void stubTopProductos() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<?> rowMapper = invocation.getArgument(1);
                    List<Object> resultado = new ArrayList<>();
                    for (int i = 0; i < productoRows.size(); i++) {
                        resultado.add(rowMapper.mapRow(productoResultSet(productoRows.get(i)), i));
                    }
                    return resultado;
                });
    }

    private ResultSet ventasResultSet() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        List<Boolean> avances = new ArrayList<>();
        for (int i = 0; i < ventasRows.size(); i++) {
            avances.add(true);
        }
        avances.add(false);
        when(rs.next()).thenAnswer(new SecuenciaDeValores<>(avances));
        when(rs.getInt("anio")).thenAnswer(new SecuenciaDeColumna<>(ventasRows, 0));
        when(rs.getInt("mes")).thenAnswer(new SecuenciaDeColumna<>(ventasRows, 1));
        when(rs.getBigDecimal("total")).thenAnswer(new SecuenciaDeColumna<>(ventasRows, 2));
        return rs;
    }

    private ResultSet productoResultSet(Map<String, Object> fila) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("id_producto")).thenReturn((Long) fila.get("id_producto"));
        when(rs.getString("nombre")).thenReturn((String) fila.get("nombre"));
        when(rs.getString("categoria")).thenReturn((String) fila.get("categoria"));
        when(rs.getLong("cantidad_vendida")).thenReturn((Long) fila.get("cantidad_vendida"));
        when(rs.getInt("stock_actual")).thenReturn((Integer) fila.get("stock_actual"));
        when(rs.getInt("stock_minimo")).thenReturn((Integer) fila.get("stock_minimo"));
        when(rs.getString("estado_producto")).thenReturn((String) fila.get("estado_producto"));
        return rs;
    }

    private Map<String, Object> productoRow(
            Long id, String nombre, String categoria, Long cantidadVendida,
            Integer stockActual, Integer stockMinimo, String estadoProducto) {
        Map<String, Object> fila = new HashMap<>();
        fila.put("id_producto", id);
        fila.put("nombre", nombre);
        fila.put("categoria", categoria);
        fila.put("cantidad_vendida", cantidadVendida);
        fila.put("stock_actual", stockActual);
        fila.put("stock_minimo", stockMinimo);
        fila.put("estado_producto", estadoProducto);
        return fila;
    }

    /** Devuelve los valores de la lista uno por invocación, repitiendo el último. */
    private static final class SecuenciaDeValores<T> implements Answer<T> {
        private final List<T> valores;
        private int indice;

        private SecuenciaDeValores(List<T> valores) {
            this.valores = valores;
        }

        @Override
        public T answer(InvocationOnMock invocation) {
            T valor = valores.get(Math.min(indice, valores.size() - 1));
            indice++;
            return valor;
        }
    }

    /** Recorre las filas simuladas devolviendo siempre la misma columna. */
    private static final class SecuenciaDeColumna<T> implements Answer<T> {
        private final List<Object[]> filas;
        private final int columna;
        private int indice;

        private SecuenciaDeColumna(List<Object[]> filas, int columna) {
            this.filas = filas;
            this.columna = columna;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T answer(InvocationOnMock invocation) {
            Object[] fila = filas.get(Math.min(indice, filas.size() - 1));
            indice++;
            return (T) fila[columna];
        }
    }
}
