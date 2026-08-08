package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.controller.support.AbstractCrudRestController;
import com.vitaNova.vitaNova.model.Radicados;
import com.vitaNova.vitaNova.repository.RadicadosRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/radicados")
public class RadicadosController extends AbstractCrudRestController<Radicados> {

    public RadicadosController(RadicadosRepository repository) {
        super(repository, Radicados::setId_radicado);
    }
}
