package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    @Query("""
            SELECT c
            FROM Compra c
            LEFT JOIN c.proveedor p
            WHERE LOWER(c.estado) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.nit) LIKE LOWER(CONCAT('%', :search, '%'))
               OR STR(c.id_compra) LIKE CONCAT('%', :search, '%')
            """)
    List<Compra> buscarCompras(
            @Param("search") String search
    );
}