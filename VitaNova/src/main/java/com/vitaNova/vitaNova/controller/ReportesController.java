package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Venta;
import com.vitaNova.vitaNova.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReportesController {

    @Autowired
    private VentaRepository ventaRepository;

    @GetMapping("/ventas")
    public List<Venta> obtenerVentas() {
        return ventaRepository.findAll();
    }
}
