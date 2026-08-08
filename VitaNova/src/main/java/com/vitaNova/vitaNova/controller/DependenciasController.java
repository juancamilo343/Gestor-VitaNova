package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Dependencias;
import com.vitaNova.vitaNova.exception.RecursoNoEncontradoException;
import com.vitaNova.vitaNova.repository.DependenciasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dependencias")
public class DependenciasController
{
    @Autowired
    private DependenciasRepository dependenciasRepository;

    @GetMapping
    public List<Dependencias> getAll()
    {
        return dependenciasRepository.findAll();
    }

    @GetMapping("/{id}")
    public Dependencias getById(@PathVariable Long id) {
        return dependenciasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dependencia", id));
    }

    @PostMapping
    public Dependencias create(@RequestBody Dependencias dependencias)
    {
        return dependenciasRepository.save(dependencias);
    }

    @PutMapping("/{id}")
    public Dependencias update(@PathVariable long id, @RequestBody Dependencias dependencias)
    {
        if (!dependenciasRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Dependencia", id);
        }
        dependencias.setId_dependencia(id);
        return dependenciasRepository.save(dependencias);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id)
    {
        if (!dependenciasRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Dependencia", id);
        }
        dependenciasRepository.deleteById(id);
    }
}