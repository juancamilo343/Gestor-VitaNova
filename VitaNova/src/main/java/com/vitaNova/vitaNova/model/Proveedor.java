package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long id_proveedor;

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "nombre_contacto", length = 100)
    private String nombre_contacto;

    @Column(name = "nit", length = 20)
    private String nit;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "direccion", length = 150)
    private String direccion;

    @Column(name = "tipo_productos_suministrados", length = 200)
    private String tipo_productos_suministrados;

    @Column(name = "estado")
    private String estado;
}