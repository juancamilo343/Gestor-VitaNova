package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "lote")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote")
    private Long id_lote;

    @Column(name = "numero_lote", length = 50)
    private String numero_lote;

    @Column(name = "fecha_fabricacion")
    private LocalDate fecha_fabricacion;

    @Column(name = "fecha_vencimiento")
    private LocalDate fecha_vencimiento;

    @NotNull(message = "La cantidad es obligatoria")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoLote estado;

    @NotNull(message = "El producto es obligatorio")
    @Column(name = "id_producto")
    private Long id_producto;

    public enum EstadoLote {
        ACTIVO,
        VENCIDO,
        RETIRADO,
        AGOTADO
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "id_producto",
            insertable = false,
            updatable = false
    )
    private Productos producto;
}