package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.ProductoPresentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoPresentacionRepository extends JpaRepository<ProductoPresentacion, Long> {

    @Query("SELECT p FROM ProductoPresentacion p WHERE p.id_producto = :idProducto AND p.estado = 'ACTIVO' ORDER BY p.es_principal DESC, p.factor_unidades ASC")
    List<ProductoPresentacion> findActivasPorProducto(@Param("idProducto") Long idProducto);

    @Query("SELECT p FROM ProductoPresentacion p WHERE p.id_producto IN :idsProductos AND p.estado = 'ACTIVO' ORDER BY p.id_producto ASC, p.es_principal DESC, p.factor_unidades ASC")
    List<ProductoPresentacion> findActivasPorProductos(@Param("idsProductos") List<Long> idsProductos);
}
