package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_venta_lote")
public class DetalleVentaLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_venta_lote")
    private Long id_detalle_venta_lote;

    @Column(name = "id_detalle_venta", nullable = false)
    private Long id_detalle_venta;

    @Column(name = "id_lote", nullable = false)
    private Long id_lote;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "id_lote",
            insertable = false,
            updatable = false
    )
    private Lote lote;

    public DetalleVentaLote() {
    }

    public Long getId_detalle_venta_lote() {
        return id_detalle_venta_lote;
    }

    public void setId_detalle_venta_lote(Long id_detalle_venta_lote) {
        this.id_detalle_venta_lote = id_detalle_venta_lote;
    }

    public Long getId_detalle_venta() {
        return id_detalle_venta;
    }

    public void setId_detalle_venta(Long id_detalle_venta) {
        this.id_detalle_venta = id_detalle_venta;
    }

    public Long getId_lote() {
        return id_lote;
    }

    public void setId_lote(Long id_lote) {
        this.id_lote = id_lote;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }
}