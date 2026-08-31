package com.vitaNova.vitaNova.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "venta")
public class Venta {

    // =====================================================
    // ID VENTA
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long id_venta;


    // =====================================================
    // FECHA
    // =====================================================

    @Column(name = "fecha")
    private LocalDateTime fecha;


    // =====================================================
    // TOTAL
    // =====================================================

    @Column(name = "total", precision = 12, scale = 2)
    private BigDecimal total;


    // =====================================================
    // DESCUENTO
    // =====================================================

    @Column(name = "descuento", precision = 12, scale = 2)
    private BigDecimal descuento;


    // =====================================================
    // IMPUESTOS
    // =====================================================

    @Column(name = "impuestos", precision = 12, scale = 2)
    private BigDecimal impuestos;


    // =====================================================
    // ESTADO
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private Estado estado;


    // =====================================================
    // CLIENTE
    // =====================================================

    @Column(name = "id_cliente")
    private Long id_cliente;


    // =====================================================
    // EMPLEADO
    // =====================================================

    @Column(name = "id_empleado")
    private Long id_empleado;


    // =====================================================
    // MÉTODO DE PAGO
    // =====================================================

    @Column(name = "id_metodo_pago")
    private Long id_metodo_pago;


    // =====================================================
    // CONSTRUCTORES
    // =====================================================

    public Venta() {
    }


    // =====================================================
    // GETTERS Y SETTERS
    // =====================================================

    public Long getId_venta() {
        return id_venta;
    }

    public void setId_venta(Long id_venta) {
        this.id_venta = id_venta;
    }


    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }


    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }


    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }


    public BigDecimal getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(BigDecimal impuestos) {
        this.impuestos = impuestos;
    }


    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }


    public Long getId_cliente() {
        return id_cliente;
    }

    public void setId_cliente(Long id_cliente) {
        this.id_cliente = id_cliente;
    }


    public Long getId_empleado() {
        return id_empleado;
    }

    public void setId_empleado(Long id_empleado) {
        this.id_empleado = id_empleado;
    }


    public Long getId_metodo_pago() {
        return id_metodo_pago;
    }

    public void setId_metodo_pago(Long id_metodo_pago) {
        this.id_metodo_pago = id_metodo_pago;
    }


    // =====================================================
    // ENUM ESTADO
    // =====================================================

    public enum Estado {
        PENDIENTE,
        PAGADA,
        ANULADA,
        DEVUELTA
    }
}