package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Long id_compra;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "El total es obligatorio")
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @NotNull(message = "El estado es obligatorio")
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @NotNull(message = "El proveedor es obligatorio")
    @Column(name = "id_proveedor", nullable = false)
    private Long id_proveedor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "id_proveedor",
            insertable = false,
            updatable = false
    )
    private Proveedor proveedor;
}