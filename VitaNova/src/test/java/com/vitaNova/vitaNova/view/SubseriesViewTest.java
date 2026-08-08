package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Subseries;
import com.vitaNova.vitaNova.repository.SeriesRepository;
import com.vitaNova.vitaNova.repository.SubseriesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubseriesViewTest {

    @Mock
    private SubseriesRepository repository;

    @Mock
    private SeriesRepository seriesRepository;

    @InjectMocks
    private SubseriesView subseriesView;

    @Test
    void listaPublicaLasSubseriesRegistradas() {
        List<Subseries> subseries = List.of(new Subseries());
        when(repository.findAll()).thenReturn(subseries);
        Model model = new ConcurrentModel();

        String vista = subseriesView.lista(model);

        assertThat(vista).isEqualTo("subseries/subseries");
        assertThat(model.getAttribute("subseries")).isEqualTo(subseries);
    }

    @Test
    void formPreparaUnaSubserieVaciaConLasSeriesDisponibles() {
        when(seriesRepository.findAll()).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String vista = subseriesView.form(model);

        assertThat(vista).isEqualTo("subseries/subseriesForm");
        assertThat(model.getAttribute("subseries")).isInstanceOf(Subseries.class);
        assertThat(model.asMap()).containsKey("seriesList");
    }

    @Test
    void saveRegistraUnaSubserieNuevaYRecargaLaVersionPersistida() {
        Subseries enviada = new Subseries();
        Subseries persistida = new Subseries();
        persistida.setId_subserie(21L);
        when(repository.save(enviada)).thenReturn(persistida);
        when(repository.findById(21L)).thenReturn(Optional.of(persistida));
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = subseriesView.save(enviada, ra);

        assertThat(destino).isEqualTo("redirect:/view/subseries");
        assertThat(flash(ra))
                .containsEntry("success", "Subserie registrada con exito")
                .containsEntry("savedSubseries", persistida);
    }

    @Test
    void saveInformaLaActualizacionCuandoLaSubserieYaTeniaId() {
        Subseries enviada = new Subseries();
        enviada.setId_subserie(8L);
        when(repository.save(enviada)).thenReturn(enviada);
        when(repository.findById(8L)).thenReturn(Optional.empty());
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        subseriesView.save(enviada, ra);

        assertThat(flash(ra)).containsEntry("success", "Subserie actualizada con exito");
    }

    @Test
    void editCargaLaSubserieSolicitada() {
        Subseries subserie = new Subseries();
        when(repository.findById(5L)).thenReturn(Optional.of(subserie));
        when(seriesRepository.findAll()).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String vista = subseriesView.edit(5L, model);

        assertThat(vista).isEqualTo("subseries/subseriesForm");
        assertThat(model.getAttribute("subseries")).isSameAs(subserie);
    }

    @Test
    void deleteEliminaLaSubserieYRedirigeAlListado() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = subseriesView.delete(6L, ra);

        verify(repository).deleteById(6L);
        assertThat(destino).isEqualTo("redirect:/view/subseries");
        assertThat(flash(ra)).containsEntry("success", "Subserie eliminada con exito");
    }

    private static Map<String, Object> flash(RedirectAttributesModelMap ra) {
        return new HashMap<>(ra.getFlashAttributes());
    }
}
