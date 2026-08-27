package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    private Long id_inventario;

    @NotNull(message = "El stock actual es obligatorio")
    @Column(name = "stock_actual", nullable = false)
    private Integer stock_actual;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Column(name = "stock_minimo", nullable = false)
    private Integer stock_minimo;

    @NotNull(message = "El stock máximo es obligatorio")
    @Column(name = "stock_maximo", nullable = false)
    private Integer stock_maximo;

    @NotNull(message = "El producto es obligatorio")
    @Column(name = "id_producto", nullable = false, unique = true)
    private Long id_producto;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "id_producto",
            insertable = false,
            updatable = false
    )
    private Productos producto;
}