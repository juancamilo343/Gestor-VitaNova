package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Clientes;
import com.vitaNova.vitaNova.repository.ClientesRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClientesController {

    @Autowired
    private ClientesRepository clientesRepository;


    // ==========================================
    // GET - TODOS LOS CLIENTES
    // ==========================================

    @GetMapping
    public List<Clientes> getAll() {

        return clientesRepository.findAll();
    }


    // ==========================================
    // GET - CLIENTE POR ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<Clientes> getById(
            @PathVariable Long id) {

        return clientesRepository
                .findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // ==========================================
    // POST - CREAR CLIENTE
    // ==========================================

    @PostMapping
    public ResponseEntity<Clientes> create(
            @Valid @RequestBody Clientes cliente) {

        Clientes nuevoCliente =
                clientesRepository.save(cliente);

        return ResponseEntity.ok(nuevoCliente);
    }


    // ==========================================
    // PUT - ACTUALIZAR CLIENTE
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<Clientes> update(
            @PathVariable Long id,
            @Valid @RequestBody Clientes cliente) {

        return clientesRepository
                .findById(id)
                .map(existing -> {

                    cliente.setId_cliente(id);

                    Clientes actualizado =
                            clientesRepository.save(cliente);

                    return ResponseEntity.ok(actualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    // ==========================================
    // DELETE - ELIMINAR CLIENTE
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        if (clientesRepository.existsById(id)) {

            clientesRepository.deleteById(id);

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}