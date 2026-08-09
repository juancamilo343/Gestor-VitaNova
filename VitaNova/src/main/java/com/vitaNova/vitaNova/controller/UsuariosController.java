package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Usuarios;
import com.vitaNova.vitaNova.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuariosController {

    @Autowired
    private UsuariosRepository usuariosRepository;

    // GET todos los usuarios
    @GetMapping
    public List<Usuarios> getAll() {
        return usuariosRepository.findAll();
    }

    // GET usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuarios> getById(@PathVariable Long id) {

        return usuariosRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST crear usuario
    @PostMapping
    public Usuarios create(@RequestBody Usuarios usuario) {
        return usuariosRepository.save(usuario);
    }

    // PUT actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<Usuarios> update(
            @PathVariable Long id,
            @RequestBody Usuarios usuario) {

        return usuariosRepository.findById(id)
                .map(existing -> {
                    usuario.setId_usuario(id);
                    return ResponseEntity.ok(
                            usuariosRepository.save(usuario)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        if (usuariosRepository.existsById(id)) {

            usuariosRepository.deleteById(id);

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}