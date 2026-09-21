package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    @Query("SELECT l FROM Lote l WHERE l.id_producto = :idProducto")
    List<Lote> findByIdProducto(@Param("idProducto") Long idProducto);

    @Query("SELECT l FROM Lote l WHERE l.id_producto = :idProducto AND l.estado = :estado")
    List<Lote> findByIdProductoAndEstado(
            @Param("idProducto") Long idProducto,
            @Param("estado") Lote.EstadoLote estado
    );

    @Query("""
        SELECT COUNT(l) > 0
        FROM Lote l
        WHERE l.id_producto = :idProducto
        AND l.numero_lote = :numeroLote
        """)
    boolean existeLotePorProducto(
            @Param("idProducto") Long idProducto,
            @Param("numeroLote") String numeroLote
    );
}