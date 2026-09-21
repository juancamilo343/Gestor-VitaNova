package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    @Query("""
            SELECT p
            FROM Proveedor p
            WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.nombre_contacto) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.nit) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.telefono) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.correo) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    List<Proveedor> buscarProveedores(
            @Param("search") String search
    );
}