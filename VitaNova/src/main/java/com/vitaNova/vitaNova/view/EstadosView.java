package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Estados;
import com.vitaNova.vitaNova.repository.EstadosRepository;
import com.vitaNova.vitaNova.view.support.AbstractCrudViewController;
import com.vitaNova.vitaNova.view.support.CrudViewDescriptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/estados")
public class EstadosView extends AbstractCrudViewController<Estados> {

    public EstadosView(EstadosRepository repository) {
        super(repository, Estados::new, Estados::getId_estado,
                new CrudViewDescriptor("/view/estados", "estados/estados", "estados/estadosForm", "estados", "Estado", false));
    }
}
