package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {

    // =====================================================
    // ID DETALLE
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_venta")
    private Long id_detalle_venta;


    // =====================================================
    // CANTIDAD
    // =====================================================

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;


    // =====================================================
    // PRECIO
    // =====================================================

    @Column(name = "precio", precision = 10, scale = 2, nullable = false)
    private BigDecimal precio;


    // =====================================================
    // SUBTOTAL
    // =====================================================

    @Column(name = "subtotal", precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal;


    // =====================================================
    // VENTA
    // =====================================================

    @Column(name = "id_venta")
    private Long id_venta;


    // =====================================================
    // PRODUCTO
    // =====================================================

    @Column(name = "id_producto")
    private Long id_producto;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public DetalleVenta() {
    }


    // =====================================================
    // GETTERS Y SETTERS
    // =====================================================

    public Long getId_detalle_venta() {
        return id_detalle_venta;
    }

    public void setId_detalle_venta(Long id_detalle_venta) {
        this.id_detalle_venta = id_detalle_venta;
    }


    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }


    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }


    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }


    public Long getId_venta() {
        return id_venta;
    }

    public void setId_venta(Long id_venta) {
        this.id_venta = id_venta;
    }


    public Long getId_producto() {
        return id_producto;
    }

    public void setId_producto(Long id_producto) {
        this.id_producto = id_producto;
    }
}