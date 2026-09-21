package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    @Query(value = """
        SELECT *
        FROM inventario
        WHERE id_producto = :idProducto
        FOR UPDATE
        """, nativeQuery = true)
    Optional<Inventario> buscarPorProductoParaActualizar(
            @Param("idProducto") Long idProducto
    );

    @Query("SELECT i FROM Inventario i WHERE i.id_producto = :idProducto")
    Optional<Inventario> buscarPorProducto(
            @Param("idProducto") Long idProducto
    );
}
