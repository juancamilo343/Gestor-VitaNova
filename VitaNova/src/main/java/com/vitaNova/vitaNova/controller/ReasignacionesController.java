package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.controller.support.AbstractCrudRestController;
import com.vitaNova.vitaNova.model.Reasignaciones;
import com.vitaNova.vitaNova.repository.ReasignacionesRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reasignaciones")
public class ReasignacionesController extends AbstractCrudRestController<Reasignaciones> {

    public ReasignacionesController(ReasignacionesRepository repository) {
        super(repository, Reasignaciones::setId_reasignacion);
    }
}
