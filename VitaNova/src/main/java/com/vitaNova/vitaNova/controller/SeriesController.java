package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Series;
import com.vitaNova.vitaNova.exception.RecursoNoEncontradoException;
import com.vitaNova.vitaNova.repository.SeriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/series")
public class SeriesController
{
    @Autowired
    private SeriesRepository repository;

    @GetMapping
    public List<Series> getAll()
    {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Series getById(@PathVariable Long id)
    {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Serie", id));
    }

    @PostMapping
    public Series create(@RequestBody Series series)
    {
        return repository.save(series);
    }

    @PutMapping("/{id}")
    public Series update(@PathVariable Long id, @RequestBody Series series)
    {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Serie", id);
        }
        series.setId_serie(id);
        return repository.save(series);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id)
    {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Serie", id);
        }
        repository.deleteById(id);
    }
}
