package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Compra;
import com.vitaNova.vitaNova.repository.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    @Autowired
    private CompraRepository compraRepository;

    // GET todas las compras
    @GetMapping
    public List<Compra> getAll() {
        return compraRepository.findAll();
    }

    // GET compra por ID
    @GetMapping("/{id}")
    public Compra getById(@PathVariable Long id) {
        return compraRepository.findById(id).orElse(null);
    }

    // POST crear compra
    @PostMapping
    public Compra create(@RequestBody Compra compra) {
        return compraRepository.save(compra);
    }

    // PUT actualizar compra
    @PutMapping("/{id}")
    public Compra update(
            @PathVariable Long id,
            @RequestBody Compra compra) {

        if (!compraRepository.existsById(id)) {
            return null;
        }

        compra.setId_compra(id);

        return compraRepository.save(compra);
    }

    // DELETE eliminar compra
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {

        if (compraRepository.existsById(id)) {
            compraRepository.deleteById(id);
        }
    }
}