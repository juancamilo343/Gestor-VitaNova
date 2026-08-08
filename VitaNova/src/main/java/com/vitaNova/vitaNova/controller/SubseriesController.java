package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.controller.support.AbstractCrudRestController;
import com.vitaNova.vitaNova.model.Subseries;
import com.vitaNova.vitaNova.repository.SubseriesRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subseries")
public class SubseriesController extends AbstractCrudRestController<Subseries> {

    public SubseriesController(SubseriesRepository repository) {
        super(repository, Subseries::setId_subserie);
    }
}
