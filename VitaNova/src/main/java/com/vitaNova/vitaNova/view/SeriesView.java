package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Series;
import com.vitaNova.vitaNova.repository.CcdUnidadRepository;
import com.vitaNova.vitaNova.repository.SeriesRepository;
import com.vitaNova.vitaNova.view.support.AbstractCrudViewController;
import com.vitaNova.vitaNova.view.support.CrudViewDescriptor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/view/series")
public class SeriesView extends AbstractCrudViewController<Series> {

    private final CcdUnidadRepository unidadesRepository;

    public SeriesView(SeriesRepository repository, CcdUnidadRepository unidadesRepository) {
        super(repository, Series::new, Series::getId_serie,
                new CrudViewDescriptor("/view/series", "series/series", "series/seriesForm", "series", "Serie", true));
        this.unidadesRepository = unidadesRepository;
    }

    @Override
    protected void populateFormModel(Model model) {
        model.addAttribute("unidadesList", unidadesRepository.findAll());
    }

    @Override
    protected void afterSave(Series saved, RedirectAttributes ra) {
        // El listado muestra el detalle del registro con la unidad ya resuelta.
        ra.addFlashAttribute("savedSeries", repository().findById(saved.getId_serie()).orElse(saved));
    }
}
