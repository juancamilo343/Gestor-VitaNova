package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Inventario;
import com.vitaNova.vitaNova.repository.InventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    @Autowired
    private InventarioRepository inventarioRepository;

    // GET todos los registros de inventario
    @GetMapping
    public List<Inventario> getAll() {
        return inventarioRepository.findAll();
    }

    // GET inventario por ID
    @GetMapping("/{id}")
    public ResponseEntity<Inventario> getById(@PathVariable Long id) {

        return inventarioRepository.findById(id)
                .map(inventario -> ResponseEntity.ok(inventario))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST crear registro de inventario
    @PostMapping
    public Inventario create(@RequestBody Inventario inventario) {
        return inventarioRepository.save(inventario);
    }

    // PUT actualizar registro de inventario
    @PutMapping("/{id}")
    public ResponseEntity<Inventario> update(
            @PathVariable Long id,
            @RequestBody Inventario inventario) {

        return inventarioRepository.findById(id)
                .map(existing -> {

                    inventario.setId_inventario(id);

                    return ResponseEntity.ok(
                            inventarioRepository.save(inventario)
                    );
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE eliminar registro de inventario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        if (inventarioRepository.existsById(id)) {

            inventarioRepository.deleteById(id);

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}