package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Documentos;
import com.vitaNova.vitaNova.exception.RecursoNoEncontradoException;
import com.vitaNova.vitaNova.repository.DocumentosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/documentos")
public class DocumentosController
{

    @Autowired
    private DocumentosRepository documentosRepository;

    @GetMapping
    public List<Documentos> getAll()
    {
        return documentosRepository.findAll();

    }

    @GetMapping("/{id}")
    public Documentos getById(@PathVariable Long id)
    {
        return documentosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Documento", id));

    }

    @PostMapping
    public Documentos create(@RequestBody Documentos Documentos)
    {
        return documentosRepository.save(Documentos);
    }

    @PutMapping("/{id}")
    public Documentos update(@PathVariable Long id, @RequestBody Documentos documentos)
    {
        if (!documentosRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Documento", id);
        }
        documentos.setId_documento(id);
        return documentosRepository.save(documentos);

    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id)
    {
        if (!documentosRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Documento", id);
        }
        documentosRepository.deleteById(id);

    }
}
