package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductosRepository extends JpaRepository<Productos, Long> {

    @Query("SELECT p FROM Productos p WHERE p.codigo_barras = :codigoBarras")
    Optional<Productos> buscarPorCodigoBarras(
            @Param("codigoBarras") String codigoBarras
    );
}