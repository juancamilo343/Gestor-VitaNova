package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "cliente",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cliente_documento",
                        columnNames = "documento"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clientes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id_cliente;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(
            max = 100,
            message = "Los nombres no pueden superar los 100 caracteres"
    )
    @Column(
            name = "nombres",
            nullable = false,
            length = 100
    )
    private String nombres;

    @Size(
            max = 100,
            message = "Los apellidos no pueden superar los 100 caracteres"
    )
    @Column(
            name = "apellidos",
            length = 100
    )
    private String apellidos;

    @NotBlank(message = "El documento es obligatorio")
    @Size(
            max = 20,
            message = "El documento no puede superar los 20 caracteres"
    )
    @Column(
            name = "documento",
            nullable = false,
            unique = true,
            length = 20
    )
    private String documento;

    @Size(
            max = 20,
            message = "El teléfono no puede superar los 20 caracteres"
    )
    @Column(
            name = "telefono",
            length = 20
    )
    private String telefono;

    @Email(
            message = "El correo electrónico debe ser válido"
    )
    @Size(
            max = 100,
            message = "El correo electrónico no puede superar los 100 caracteres"
    )
    @Column(
            name = "correo",
            length = 100
    )
    private String correo;

    @Size(
            max = 150,
            message = "La dirección no puede superar los 150 caracteres"
    )
    @Column(
            name = "direccion",
            length = 150
    )
    private String direccion;

    @NotNull(
            message = "La fecha de nacimiento es obligatoria"
    )
    @Past(
            message = "La fecha de nacimiento debe ser anterior a la fecha actual"
    )
    @Column(name = "fecha_nacimiento")
    private LocalDate fecha_nacimiento;

    @Size(
            max = 100,
            message = "La EPS no puede superar los 100 caracteres"
    )
    @Column(
            name = "eps",
            length = 100
    )
    private String eps;

    @Column(
            name = "alergias",
            columnDefinition = "TEXT"
    )
    private String alergias;

    @Column(name = "fecha_registro")
    private LocalDate fecha_registro;
}