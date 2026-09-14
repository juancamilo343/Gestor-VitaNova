package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Clientes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientesRepository extends JpaRepository<Clientes, Long> {

    @Query("""
            SELECT COUNT(c) > 0
            FROM Clientes c
            WHERE c.documento = :documento
            """)
    boolean existeDocumento(@Param("documento") String documento);
}