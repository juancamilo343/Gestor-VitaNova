package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Subseries;
import com.vitaNova.vitaNova.exception.RecursoNoEncontradoException;
import com.vitaNova.vitaNova.repository.SubseriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subseries")
public class SubseriesController
{
    @Autowired
    private SubseriesRepository repository;

    @GetMapping
    public List<Subseries> getAll()
    {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Subseries getById(@PathVariable Long id)
    {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subserie", id));
    }

    @PostMapping
    public Subseries create(@RequestBody Subseries subseries)
    {
        return repository.save(subseries);
    }

    @PutMapping("/{id}")
    public Subseries update(@PathVariable Long id, @RequestBody Subseries subseries)
    {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Subserie", id);
        }
        subseries.setId_subserie(id);
        return repository.save(subseries);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id)
    {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Subserie", id);
        }
        repository.deleteById(id);
    }
}
