package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Productos;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductosController {

    @Autowired
    private ProductosRepository productosRepository;

    // GET todos los productos
    @GetMapping
    public List<Productos> getAll() {
        return productosRepository.findAll();
    }

    // GET producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Productos> getById(@PathVariable Long id) {

        return productosRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST crear producto
    @PostMapping
    public Productos create(@RequestBody Productos producto) {
        return productosRepository.save(producto);
    }

    // PUT actualizar producto
    @PutMapping("/{id}")
    public ResponseEntity<Productos> update(
            @PathVariable Long id,
            @RequestBody Productos producto) {

        return productosRepository.findById(id)
                .map(existing -> {
                    producto.setId_producto(id);
                    return ResponseEntity.ok(
                            productosRepository.save(producto)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE eliminar producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        if (productosRepository.existsById(id)) {

            productosRepository.deleteById(id);

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}