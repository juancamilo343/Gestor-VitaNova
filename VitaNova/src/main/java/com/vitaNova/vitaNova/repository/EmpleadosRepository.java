package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Empleados;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpleadosRepository extends JpaRepository<Empleados, Long> {

    @Query("""
            SELECT COUNT(e) > 0
            FROM Empleados e
            WHERE e.documento = :documento
            """)
    boolean existeDocumento(@Param("documento") String documento);
}