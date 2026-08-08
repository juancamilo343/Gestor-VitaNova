package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.controller.support.AbstractCrudRestController;
import com.vitaNova.vitaNova.model.Documentos;
import com.vitaNova.vitaNova.repository.DocumentosRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documentos")
public class DocumentosController extends AbstractCrudRestController<Documentos> {

    public DocumentosController(DocumentosRepository repository) {
        super(repository, Documentos::setId_documento);
    }
}
