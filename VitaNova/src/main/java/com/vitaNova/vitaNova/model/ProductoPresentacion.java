package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "producto_presentacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoPresentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_presentacion")
    private Long id_presentacion;

    @NotNull(message = "El producto es obligatorio")
    @Column(name = "id_producto", nullable = false)
    private Long id_producto;

    @NotBlank(message = "El nombre de la presentación es obligatorio")
    @Column(name = "nombre_presentacion", nullable = false, length = 50)
    private String nombre_presentacion;

    @NotNull(message = "El factor de unidades es obligatorio")
    @Column(name = "factor_unidades", nullable = false)
    @Builder.Default
    private Integer factor_unidades = 1;

    @NotNull(message = "El precio de venta es obligatorio")
    @Column(name = "precio_venta", precision = 10, scale = 2, nullable = false)
    private BigDecimal precio_venta;

    @Column(name = "es_principal")
    @Builder.Default
    private Boolean es_principal = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private EstadoPresentacion estado = EstadoPresentacion.ACTIVO;

    public enum EstadoPresentacion {
        ACTIVO,
        INACTIVO
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", insertable = false, updatable = false)
    private Productos producto;
}
