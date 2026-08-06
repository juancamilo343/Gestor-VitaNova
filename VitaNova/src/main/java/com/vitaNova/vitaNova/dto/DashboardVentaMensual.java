package com.vitaNova.vitaNova.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class DashboardVentaMensual {

    private Integer anio;
    private String mes;
    private BigDecimal total;
    private String totalTexto;
    private Integer porcentaje;
}
