package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.ProductoPresentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoPresentacionRepository extends JpaRepository<ProductoPresentacion, Long> {

    @Query("SELECT p FROM ProductoPresentacion p WHERE p.id_producto = :idProducto ORDER BY p.factor_unidades ASC, p.id_presentacion ASC")
    List<ProductoPresentacion> findPorProducto(@Param("idProducto") Long idProducto);

    @Query("SELECT p FROM ProductoPresentacion p WHERE p.id_producto = :idProducto AND p.estado = 'ACTIVO' ORDER BY p.es_principal DESC, p.factor_unidades ASC")
    List<ProductoPresentacion> findActivasPorProducto(@Param("idProducto") Long idProducto);

    @Query("SELECT p FROM ProductoPresentacion p WHERE p.id_producto = :idProducto AND p.estado = 'ACTIVO' AND p.permite_venta = true ORDER BY p.es_principal DESC, p.factor_unidades ASC")
    List<ProductoPresentacion> findActivasVentaPorProducto(@Param("idProducto") Long idProducto);

    @Query("SELECT p FROM ProductoPresentacion p WHERE p.id_producto = :idProducto AND p.estado = 'ACTIVO' AND p.permite_compra = true ORDER BY p.es_principal DESC, p.factor_unidades ASC")
    List<ProductoPresentacion> findActivasCompraPorProducto(@Param("idProducto") Long idProducto);

    @Query("SELECT p FROM ProductoPresentacion p WHERE p.id_producto IN :idsProductos AND p.estado = 'ACTIVO' AND p.permite_venta = true ORDER BY p.id_producto ASC, p.es_principal DESC, p.factor_unidades ASC")
    List<ProductoPresentacion> findActivasVentaPorProductos(@Param("idsProductos") List<Long> idsProductos);

    @Query("SELECT p FROM ProductoPresentacion p WHERE p.id_producto IN :idsProductos AND p.estado = 'ACTIVO' AND p.permite_compra = true ORDER BY p.id_producto ASC, p.es_principal DESC, p.factor_unidades ASC")
    List<ProductoPresentacion> findActivasCompraPorProductos(@Param("idsProductos") List<Long> idsProductos);
}
