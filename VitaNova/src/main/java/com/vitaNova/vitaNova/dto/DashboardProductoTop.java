package com.vitaNova.vitaNova.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DashboardProductoTop {

    private Long idProducto;
    private String nombre;
    private String categoria;
    private Long cantidadVendida;
    private String cantidadVendidaTexto;
    private Integer stockActual;
    private Integer stockMinimo;
    private String estado;
    private String estadoClase;
}
