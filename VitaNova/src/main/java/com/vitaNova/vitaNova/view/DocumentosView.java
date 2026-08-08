package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Documentos;
import com.vitaNova.vitaNova.repository.DocumentosRepository;
import com.vitaNova.vitaNova.view.support.AbstractCrudViewController;
import com.vitaNova.vitaNova.view.support.CrudViewDescriptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/documentos")
public class DocumentosView extends AbstractCrudViewController<Documentos> {

    public DocumentosView(DocumentosRepository repository) {
        super(repository, Documentos::new, Documentos::getId_documento,
                new CrudViewDescriptor("/view/documentos", "documentos/documentos", "documentos/documentosForm", "documentos", "Documento", false));
    }
}
