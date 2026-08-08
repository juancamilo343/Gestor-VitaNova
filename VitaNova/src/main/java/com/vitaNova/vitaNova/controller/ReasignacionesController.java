package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Reasignaciones;
import com.vitaNova.vitaNova.exception.RecursoNoEncontradoException;
import com.vitaNova.vitaNova.repository.ReasignacionesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reasignaciones")
public class ReasignacionesController {

    @Autowired
    private ReasignacionesRepository reasignacionesRepository;

    @GetMapping
    public List<Reasignaciones> getAll() {
        return reasignacionesRepository.findAll();
    }

    @GetMapping("/{id}")
    public Reasignaciones getById(@PathVariable Long id) {
        return reasignacionesRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reasignacion", id));
    }

    @PostMapping
    public Reasignaciones create(@RequestBody Reasignaciones reasignaciones) {
        return reasignacionesRepository.save(reasignaciones);
    }

    @PutMapping("/{id}")
    public Reasignaciones update(@PathVariable Long id, @RequestBody Reasignaciones reasignaciones) {
        if (!reasignacionesRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Reasignacion", id);
        }
        reasignaciones.setId_reasignacion(id);
        return reasignacionesRepository.save(reasignaciones);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        if (!reasignacionesRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Reasignacion", id);
        }
        reasignacionesRepository.deleteById(id);
    }
}