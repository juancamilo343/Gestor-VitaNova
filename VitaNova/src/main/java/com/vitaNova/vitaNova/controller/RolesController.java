package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.controller.support.AbstractCrudRestController;
import com.vitaNova.vitaNova.model.Roles;
import com.vitaNova.vitaNova.repository.RolesRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RolesController extends AbstractCrudRestController<Roles> {

    public RolesController(RolesRepository repository) {
        super(repository, Roles::setId_rol);
    }
}
