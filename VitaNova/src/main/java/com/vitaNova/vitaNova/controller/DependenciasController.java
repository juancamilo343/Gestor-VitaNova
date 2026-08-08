package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.controller.support.AbstractCrudRestController;
import com.vitaNova.vitaNova.model.Dependencias;
import com.vitaNova.vitaNova.repository.DependenciasRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dependencias")
public class DependenciasController extends AbstractCrudRestController<Dependencias> {

    public DependenciasController(DependenciasRepository repository) {
        super(repository, Dependencias::setId_dependencia);
    }
}
