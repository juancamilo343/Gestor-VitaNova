package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Proveedor;
import com.vitaNova.vitaNova.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorRepository proveedorRepository;

    // GET todos los proveedores
    @GetMapping
    public List<Proveedor> getAll() {
        return proveedorRepository.findAll();
    }

    // GET proveedor por ID
    @GetMapping("/{id}")
    public ResponseEntity<Proveedor> getById(@PathVariable Long id) {

        return proveedorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST crear proveedor
    @PostMapping
    public Proveedor create(@RequestBody Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    // PUT actualizar proveedor
    @PutMapping("/{id}")
    public ResponseEntity<Proveedor> update(
            @PathVariable Long id,
            @RequestBody Proveedor proveedor) {

        return proveedorRepository.findById(id)
                .map(existing -> {
                    proveedor.setId_proveedor(id);
                    return ResponseEntity.ok(
                            proveedorRepository.save(proveedor)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE eliminar proveedor
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        if (proveedorRepository.existsById(id)) {

            proveedorRepository.deleteById(id);

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}