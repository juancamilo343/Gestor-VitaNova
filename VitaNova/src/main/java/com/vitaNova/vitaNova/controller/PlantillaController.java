package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.controller.support.AbstractCrudRestController;
import com.vitaNova.vitaNova.model.Plantilla;
import com.vitaNova.vitaNova.repository.PlantillaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plantilla")
public class PlantillaController extends AbstractCrudRestController<Plantilla> {

    public PlantillaController(PlantillaRepository repository) {
        super(repository, Plantilla::setId_evento);
    }
}
