package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.controller.support.AbstractCrudRestController;
import com.vitaNova.vitaNova.model.Series;
import com.vitaNova.vitaNova.repository.SeriesRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/series")
public class SeriesController extends AbstractCrudRestController<Series> {

    public SeriesController(SeriesRepository repository) {
        super(repository, Series::setId_serie);
    }
}
