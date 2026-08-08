package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.controller.support.AbstractCrudRestController;
import com.vitaNova.vitaNova.model.Tramites;
import com.vitaNova.vitaNova.repository.TramitesRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tramites")
public class TramitesController extends AbstractCrudRestController<Tramites> {

    public TramitesController(TramitesRepository repository) {
        super(repository, Tramites::setIdTramite);
    }
}
