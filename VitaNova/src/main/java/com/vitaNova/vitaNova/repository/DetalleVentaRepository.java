package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    @Query("SELECT detalle FROM DetalleVenta detalle " +
            "WHERE detalle.id_venta = :idVenta")
    List<DetalleVenta> buscarPorVenta(@Param("idVenta") Long idVenta);
}
