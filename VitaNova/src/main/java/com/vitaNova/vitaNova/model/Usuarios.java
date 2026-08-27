package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id_usuario;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Column(
            name = "username",
            nullable = false,
            length = 50
    )
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(
            name = "password",
            nullable = false,
            length = 255
    )
    private String password;

    @NotNull(message = "El estado es obligatorio")
    @Column(name = "estado")
    private Boolean estado;

    @NotNull(message = "El rol es obligatorio")
    @Column(name = "id_rol")
    private Long id_rol;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "id_rol",
            insertable = false,
            updatable = false
    )
    private Rol rol;
}