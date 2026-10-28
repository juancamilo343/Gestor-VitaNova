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
    // PRESENTACIÓN FARMACÉUTICA
    // =====================================================

    @Column(name = "id_presentacion")
    private Long id_presentacion;

    @Column(name = "nombre_presentacion", length = 50)
    private String nombre_presentacion;

    @Column(name = "factor_unidades", nullable = false)
    private Integer factor_unidades = 1;

    @Column(name = "unidades_base")
    private Integer unidades_base;


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


    public Long getId_presentacion() {
        return id_presentacion;
    }

    public void setId_presentacion(Long id_presentacion) {
        this.id_presentacion = id_presentacion;
    }


    public String getNombre_presentacion() {
        return nombre_presentacion;
    }

    public void setNombre_presentacion(String nombre_presentacion) {
        this.nombre_presentacion = nombre_presentacion;
    }


    public Integer getFactor_unidades() {
        return factor_unidades != null ? factor_unidades : 1;
    }

    public void setFactor_unidades(Integer factor_unidades) {
        this.factor_unidades = factor_unidades;
    }


    public Integer getUnidades_base() {
        return unidades_base;
    }

    public void setUnidades_base(Integer unidades_base) {
        this.unidades_base = unidades_base;
    }
}
