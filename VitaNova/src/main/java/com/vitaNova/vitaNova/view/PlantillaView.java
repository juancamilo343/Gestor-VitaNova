package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Plantilla;
import com.vitaNova.vitaNova.repository.PlantillaRepository;
import com.vitaNova.vitaNova.view.support.AbstractCrudViewController;
import com.vitaNova.vitaNova.view.support.CrudViewDescriptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/plantilla")
public class PlantillaView extends AbstractCrudViewController<Plantilla> {

    public PlantillaView(PlantillaRepository repository) {
        super(repository, Plantilla::new, Plantilla::getId_evento,
                new CrudViewDescriptor("/view/plantilla", "Plantilla/Plantilla", "Plantilla/PlantillaForm", "plantilla", "Plantilla", true));
    }
}
