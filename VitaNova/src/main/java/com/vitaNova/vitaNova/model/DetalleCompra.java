package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_compra")
public class DetalleCompra {

    // =====================================================
    // ID DETALLE COMPRA
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_compra")
    private Long id_detalle_compra;


    // =====================================================
    // CANTIDAD
    // =====================================================

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;


    // =====================================================
    // PRECIO
    // =====================================================

    @Column(
            name = "precio",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal precio;


    // =====================================================
    // SUBTOTAL
    // =====================================================

    @Column(
            name = "subtotal",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal subtotal;


    // =====================================================
    // ID COMPRA
    // =====================================================

    @Column(name = "id_compra")
    private Long id_compra;


    // =====================================================
    // ID PRODUCTO
    // =====================================================

    @Column(name = "id_producto")
    private Long id_producto;


    // =====================================================
    // CONSTRUCTOR VACÍO
    // =====================================================

    public DetalleCompra() {
    }


    // =====================================================
    // GETTER Y SETTER - ID DETALLE COMPRA
    // =====================================================

    public Long getId_detalle_compra() {
        return id_detalle_compra;
    }

    public void setId_detalle_compra(Long id_detalle_compra) {
        this.id_detalle_compra = id_detalle_compra;
    }


    // =====================================================
    // GETTER Y SETTER - CANTIDAD
    // =====================================================

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }


    // =====================================================
    // GETTER Y SETTER - PRECIO
    // =====================================================

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }


    // =====================================================
    // GETTER Y SETTER - SUBTOTAL
    // =====================================================

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }


    // =====================================================
    // GETTER Y SETTER - ID COMPRA
    // =====================================================

    public Long getId_compra() {
        return id_compra;
    }

    public void setId_compra(Long id_compra) {
        this.id_compra = id_compra;
    }


    // =====================================================
    // GETTER Y SETTER - ID PRODUCTO
    // =====================================================

    public Long getId_producto() {
        return id_producto;
    }

    public void setId_producto(Long id_producto) {
        this.id_producto = id_producto;
    }
}