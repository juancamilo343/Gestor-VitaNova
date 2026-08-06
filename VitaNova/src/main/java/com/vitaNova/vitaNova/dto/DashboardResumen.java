package com.vitaNova.vitaNova.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class DashboardResumen {

    private String totalProductosTexto;
    private String ventasHoyTexto;
    private String stockBajoTexto;
    private String clientesRegistradosTexto;
    private String proveedoresRegistradosTexto;
    private String empleadosRegistradosTexto;
    private String facturasEmitidasTexto;
    private String comprasPendientesTexto;
    private String lotesPorVencerTexto;
    private String devolucionesHoyTexto;

    private String ventasMensualesTexto;
    private String coberturaInventarioMensaje;
    private Integer coberturaInventarioPorcentaje;

    private String chartLinePoints;
    private String chartAreaPoints;

    private List<DashboardVentaMensual> ventasMensuales = new ArrayList<>();
    private List<DashboardProductoTop> topProductos = new ArrayList<>();
    private List<DashboardAlerta> alertas = new ArrayList<>();
}
