package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Reasignaciones;
import com.vitaNova.vitaNova.repository.ReasignacionesRepository;
import com.vitaNova.vitaNova.view.support.AbstractCrudViewController;
import com.vitaNova.vitaNova.view.support.CrudViewDescriptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/reasignaciones")
public class ReasignacionesView extends AbstractCrudViewController<Reasignaciones> {

    public ReasignacionesView(ReasignacionesRepository repository) {
        super(repository, Reasignaciones::new, Reasignaciones::getId_reasignacion,
                new CrudViewDescriptor("/view/reasignaciones", "reasignaciones/reasignaciones", "reasignaciones/reasignacionesForm", "reasignaciones", "Reasignacion", true));
    }
}
