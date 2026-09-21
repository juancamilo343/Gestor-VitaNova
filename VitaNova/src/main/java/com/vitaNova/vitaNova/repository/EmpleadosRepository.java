package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Empleados;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmpleadosRepository extends JpaRepository<Empleados, Long> {

    // ==========================================================
    // VALIDAR DOCUMENTO
    // ==========================================================
    @Query("""
            SELECT COUNT(e) > 0
            FROM Empleados e
            WHERE e.documento = :documento
            """)
    boolean existeDocumento(
            @Param("documento") String documento
    );


    // ==========================================================
    // BUSCAR EMPLEADO POR USUARIO
    // ==========================================================
    @Query("""
            SELECT e
            FROM Empleados e
            WHERE e.id_usuario = :idUsuario
            """)
    Optional<Empleados> buscarPorIdUsuario(
            @Param("idUsuario") Long idUsuario
    );


    // ==========================================================
    // BUSCAR EMPLEADOS
    // POR NOMBRE, APELLIDO O DOCUMENTO
    // ==========================================================
    @Query("""
            SELECT e
            FROM Empleados e
            WHERE LOWER(e.nombres) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(e.documento) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    List<Empleados> buscarEmpleados(
            @Param("search") String search
    );
}