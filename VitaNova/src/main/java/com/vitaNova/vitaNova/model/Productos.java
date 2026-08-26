package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Productos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id_producto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "codigo_interno", length = 50)
    private String codigo_interno;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_compra", precision = 10, scale = 2)
    private BigDecimal precio_compra;

    @Column(name = "precio_venta", precision = 10, scale = 2)
    private BigDecimal precio_venta;

    @Column(name = "codigo_barras", length = 50)
    private String codigo_barras;

    @Column(name = "ubicacion", length = 100)
    private String ubicacion;

    @NotNull(message = "El estado es obligatorio")
    @Column(name = "estado")
    private String estado;

    @NotNull(message = "La categoría es obligatoria")
    @Column(name = "id_categoria")
    private Long id_categoria;

    @NotNull(message = "El proveedor es obligatorio")
    @Column(name = "id_proveedor")
    private Long id_proveedor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria", insertable = false, updatable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_proveedor", insertable = false, updatable = false)
    private Proveedor proveedor;
}