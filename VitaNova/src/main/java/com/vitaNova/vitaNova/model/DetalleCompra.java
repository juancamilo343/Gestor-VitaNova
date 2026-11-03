package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_compra")
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_compra")
    private Long id_detalle_compra;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(
            name = "precio",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal precio;

    @Column(
            name = "subtotal",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal subtotal;

    @Column(name = "id_compra")
    private Long id_compra;

    @Column(name = "id_producto")
    private Long id_producto;

    @Column(name = "id_lote")
    private Long id_lote;

    @Column(name = "id_presentacion")
    private Long id_presentacion;

    @Column(name = "nombre_presentacion", length = 50)
    private String nombre_presentacion;

    @Column(name = "factor_unidades", nullable = false)
    private Integer factor_unidades = 1;

    @Column(name = "unidades_base")
    private Integer unidades_base;

    @Column(name = "costo_unitario_base", precision = 10, scale = 2)
    private BigDecimal costo_unitario_base;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "id_lote",
            insertable = false,
            updatable = false
    )
    private Lote lote;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "id_producto",
            insertable = false,
            updatable = false
    )
    private Productos producto;

    public DetalleCompra() {
    }

    public Long getId_detalle_compra() {
        return id_detalle_compra;
    }

    public void setId_detalle_compra(Long id_detalle_compra) {
        this.id_detalle_compra = id_detalle_compra;
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

    public Long getId_compra() {
        return id_compra;
    }

    public void setId_compra(Long id_compra) {
        this.id_compra = id_compra;
    }

    public Long getId_producto() {
        return id_producto;
    }

    public void setId_producto(Long id_producto) {
        this.id_producto = id_producto;
    }

    public Long getId_lote() {
        return id_lote;
    }

    public void setId_lote(Long id_lote) {
        this.id_lote = id_lote;
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

    public BigDecimal getCosto_unitario_base() {
        return costo_unitario_base;
    }

    public void setCosto_unitario_base(BigDecimal costo_unitario_base) {
        this.costo_unitario_base = costo_unitario_base;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public Productos getProducto() {
        return producto;
    }

    public void setProducto(Productos producto) {
        this.producto = producto;
    }

    public BigDecimal getPrecio_compra() {
        return this.precio;
    }
}
