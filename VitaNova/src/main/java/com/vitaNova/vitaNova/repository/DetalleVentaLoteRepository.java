package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.DetalleVentaLote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaLoteRepository extends JpaRepository<DetalleVentaLote, Long> {

    @Query("""
        SELECT dvl
        FROM DetalleVentaLote dvl
        WHERE dvl.id_detalle_venta = :idDetalleVenta
        """)
    List<DetalleVentaLote> buscarPorDetalleVenta(
            @Param("idDetalleVenta") Long idDetalleVenta
    );

    @Query("""
        SELECT dvl
        FROM DetalleVentaLote dvl
        WHERE dvl.id_lote = :idLote
        """)
    List<DetalleVentaLote> buscarPorLote(
            @Param("idLote") Long idLote
    );

    @Query("""
        DELETE FROM DetalleVentaLote dvl
        WHERE dvl.id_detalle_venta = :idDetalleVenta
        """)
    void eliminarPorDetalleVenta(
            @Param("idDetalleVenta") Long idDetalleVenta
    );
}
