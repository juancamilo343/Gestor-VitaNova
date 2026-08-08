package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Tramites;
import com.vitaNova.vitaNova.repository.DependenciasRepository;
import com.vitaNova.vitaNova.repository.EstadosRepository;
import com.vitaNova.vitaNova.repository.RadicadosRepository;
import com.vitaNova.vitaNova.repository.TramitesRepository;
import com.vitaNova.vitaNova.repository.UsuariosRepository;
import com.vitaNova.vitaNova.view.support.AbstractCrudViewController;
import com.vitaNova.vitaNova.view.support.CrudViewDescriptor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/tramites")
public class TramitesView extends AbstractCrudViewController<Tramites> {

    private final RadicadosRepository radicadosRepository;
    private final EstadosRepository estadosRepository;
    private final DependenciasRepository dependenciasRepository;
    private final UsuariosRepository usuariosRepository;

    public TramitesView(TramitesRepository repository,
                        RadicadosRepository radicadosRepository,
                        EstadosRepository estadosRepository,
                        DependenciasRepository dependenciasRepository,
                        UsuariosRepository usuariosRepository) {
        super(repository, Tramites::new, Tramites::getIdTramite,
                new CrudViewDescriptor("/view/tramites", "tramites/tramites", "tramites/tramitesForm",
                        "tramites", "Tramite", false));
        this.radicadosRepository = radicadosRepository;
        this.estadosRepository = estadosRepository;
        this.dependenciasRepository = dependenciasRepository;
        this.usuariosRepository = usuariosRepository;
    }

    @Override
    protected void populateListModel(Model model) {
        model.addAttribute("radicados", radicadosRepository.findAll());
        model.addAttribute("estados", estadosRepository.findAll());
        model.addAttribute("dependencias", dependenciasRepository.findAll());
        model.addAttribute("usuarios", usuariosRepository.findAll());

        model.addAttribute("totalTramites", repository().count());
        model.addAttribute("pendientes", radicadosRepository.countPendientes());
        model.addAttribute("enProceso", radicadosRepository.countEnTramite());
        model.addAttribute("finalizados", radicadosRepository.countFinalizados());
        model.addAttribute("vencidos", radicadosRepository.countVencidos());
    }

    @Override
    protected void populateFormModel(Model model) {
        model.addAttribute("estados", estadosRepository.findAll());
        model.addAttribute("dependencias", dependenciasRepository.findAll());
    }
}
