package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Categoria;
import com.vitaNova.vitaNova.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // GET todas las categorías
    @GetMapping
    public List<Categoria> getAll() {
        return categoriaRepository.findAll();
    }

    // GET categoría por ID
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> getById(@PathVariable Long id) {

        return categoriaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST crear categoría
    @PostMapping
    public Categoria create(@RequestBody Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    // PUT actualizar categoría
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> update(
            @PathVariable Long id,
            @RequestBody Categoria categoria) {

        return categoriaRepository.findById(id)
                .map(existing -> {
                    categoria.setId_categoria(id);
                    return ResponseEntity.ok(
                            categoriaRepository.save(categoria)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE eliminar categoría
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        if (categoriaRepository.existsById(id)) {

            categoriaRepository.deleteById(id);

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}