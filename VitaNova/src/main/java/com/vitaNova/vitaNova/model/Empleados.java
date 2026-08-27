package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "empleado",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_empleado_documento",
                        columnNames = "documento"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empleados {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Long id_empleado;


    // =========================================================
    // INFORMACIÓN PERSONAL
    // =========================================================

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


    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(
            max = 100,
            message = "Los apellidos no pueden superar los 100 caracteres"
    )
    @Column(
            name = "apellidos",
            nullable = false,
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


    // =========================================================
    // INFORMACIÓN DE CONTACTO
    // =========================================================

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
            message = "El correo no puede superar los 100 caracteres"
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


    // =========================================================
    // INFORMACIÓN LABORAL
    // =========================================================

    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "El salario no puede ser negativo"
    )
    @Column(
            name = "salario",
            precision = 10,
            scale = 2
    )
    private BigDecimal salario;


    @PastOrPresent(
            message = "La fecha de ingreso no puede ser futura"
    )
    @Column(name = "fecha_ingreso")
    private LocalDate fecha_ingreso;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            columnDefinition = "ENUM('ACTIVO','INACTIVO')"
    )
    @Builder.Default
    private EstadoEmpleado estado = EstadoEmpleado.ACTIVO;


    // =========================================================
    // RELACIÓN CON USUARIO
    // =========================================================

    @Column(name = "id_usuario")
    private Long id_usuario;


    // =========================================================
    // ESTADO DEL EMPLEADO
    // =========================================================

    public enum EstadoEmpleado {

        ACTIVO,
        INACTIVO

    }
}