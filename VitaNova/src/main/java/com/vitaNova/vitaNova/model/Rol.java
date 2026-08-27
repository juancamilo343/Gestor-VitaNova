package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long id_rol;

    @Column(
            name = "nombre",
            nullable = false,
            length = 50
    )
    private String nombre;

    @Column(
            name = "descripcion",
            length = 150
    )
    private String descripcion;

    @Column(
            name = "salario",
            precision = 10,
            scale = 2
    )
    private BigDecimal salario;
}