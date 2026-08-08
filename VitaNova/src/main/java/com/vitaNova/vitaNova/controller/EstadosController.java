package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.controller.support.AbstractCrudRestController;
import com.vitaNova.vitaNova.model.Estados;
import com.vitaNova.vitaNova.repository.EstadosRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estados")
public class EstadosController extends AbstractCrudRestController<Estados> {

    public EstadosController(EstadosRepository repository) {
        super(repository, Estados::setId_estado);
    }
}
