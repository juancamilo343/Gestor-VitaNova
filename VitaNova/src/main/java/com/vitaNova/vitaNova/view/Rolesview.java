package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Roles;
import com.vitaNova.vitaNova.repository.RolesRepository;
import com.vitaNova.vitaNova.view.support.AbstractCrudViewController;
import com.vitaNova.vitaNova.view.support.CrudViewDescriptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/roles")
public class Rolesview extends AbstractCrudViewController<Roles> {

    public Rolesview(RolesRepository repository) {
        super(repository, Roles::new, Roles::getId_rol,
                new CrudViewDescriptor("/view/roles", "roles/roles", "roles/rolesForm", "roles", "Rol", false));
    }
}
