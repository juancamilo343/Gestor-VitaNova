package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Clientes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClientesRepository extends JpaRepository<Clientes, Long> {

    // ==========================================================
    // VALIDAR DOCUMENTO
    // ==========================================================
    @Query("""
            SELECT COUNT(c) > 0
            FROM Clientes c
            WHERE c.documento = :documento
            """)
    boolean existeDocumento(
            @Param("documento") String documento
    );


    // ==========================================================
    // BUSCAR CLIENTES
    // POR NOMBRE, APELLIDO O DOCUMENTO
    // ==========================================================
    @Query("""
            SELECT c
            FROM Clientes c
            WHERE LOWER(c.nombres) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(c.documento) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    List<Clientes> buscarClientes(
            @Param("search") String search
    );
}