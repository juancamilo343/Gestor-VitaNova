package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Dependencias;
import com.vitaNova.vitaNova.repository.DependenciasRepository;
import com.vitaNova.vitaNova.view.support.AbstractCrudViewController;
import com.vitaNova.vitaNova.view.support.CrudViewDescriptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/dependencias")
public class Dependenciasview extends AbstractCrudViewController<Dependencias> {

    public Dependenciasview(DependenciasRepository repository) {
        super(repository, Dependencias::new, Dependencias::getId_dependencia,
                new CrudViewDescriptor("/view/dependencias", "dependencias/dependencias", "dependencias/dependenciasForm", "dependencias", "Dependencia", true));
    }
}
