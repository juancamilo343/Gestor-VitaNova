package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Subseries;
import com.vitaNova.vitaNova.repository.SeriesRepository;
import com.vitaNova.vitaNova.repository.SubseriesRepository;
import com.vitaNova.vitaNova.view.support.AbstractCrudViewController;
import com.vitaNova.vitaNova.view.support.CrudViewDescriptor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/view/subseries")
public class SubseriesView extends AbstractCrudViewController<Subseries> {

    private final SeriesRepository seriesRepository;

    public SubseriesView(SubseriesRepository repository, SeriesRepository seriesRepository) {
        super(repository, Subseries::new, Subseries::getId_subserie,
                new CrudViewDescriptor("/view/subseries", "subseries/subseries", "subseries/subseriesForm",
                        "subseries", "Subserie", true));
        this.seriesRepository = seriesRepository;
    }

    @Override
    protected void populateFormModel(Model model) {
        model.addAttribute("seriesList", seriesRepository.findAll());
    }

    @Override
    protected void afterSave(Subseries saved, RedirectAttributes ra) {
        // El listado muestra el detalle del registro con la serie ya resuelta.
        ra.addFlashAttribute("savedSubseries", repository().findById(saved.getId_subserie()).orElse(saved));
    }
}
