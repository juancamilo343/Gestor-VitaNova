package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Empleados;
import com.vitaNova.vitaNova.repository.EmpleadosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadosController {

    @Autowired
    private EmpleadosRepository empleadosRepository;

    // =========================
    // GET - TODOS LOS EMPLEADOS
    // =========================
    @GetMapping
    public List<Empleados> getAll() {
        return empleadosRepository.findAll();
    }

    // =========================
    // GET - EMPLEADO POR ID
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<Empleados> getById(
            @PathVariable Long id) {

        return empleadosRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================
    // POST - CREAR EMPLEADO
    // =========================
    @PostMapping
    public Empleados create(
            @RequestBody Empleados empleado) {

        return empleadosRepository.save(empleado);
    }

    // =========================
    // PUT - ACTUALIZAR EMPLEADO
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<Empleados> update(
            @PathVariable Long id,
            @RequestBody Empleados empleado) {

        return empleadosRepository.findById(id)
                .map(existing -> {

                    empleado.setId_empleado(id);

                    return ResponseEntity.ok(
                            empleadosRepository.save(empleado)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================
    // DELETE - ELIMINAR EMPLEADO
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        if (empleadosRepository.existsById(id)) {

            empleadosRepository.deleteById(id);

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}