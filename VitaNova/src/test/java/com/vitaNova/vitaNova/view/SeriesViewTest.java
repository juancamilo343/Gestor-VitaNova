package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Series;
import com.vitaNova.vitaNova.repository.CcdUnidadRepository;
import com.vitaNova.vitaNova.repository.SeriesRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeriesViewTest {

    @Mock
    private SeriesRepository repository;

    @Mock
    private CcdUnidadRepository unidadesRepository;

    @InjectMocks
    private SeriesView seriesView;

    @Test
    void listaPublicaLasSeriesRegistradas() {
        List<Series> series = List.of(new Series());
        when(repository.findAll()).thenReturn(series);
        Model model = new ConcurrentModel();

        String vista = seriesView.lista(model);

        assertThat(vista).isEqualTo("series/series");
        assertThat(model.getAttribute("series")).isEqualTo(series);
    }

    @Test
    void formPreparaUnaSerieVaciaConLasUnidadesDisponibles() {
        when(unidadesRepository.findAll()).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String vista = seriesView.form(model);

        assertThat(vista).isEqualTo("series/seriesForm");
        assertThat(model.getAttribute("series")).isInstanceOf(Series.class);
        assertThat(model.asMap()).containsKey("unidadesList");
    }

    @Test
    void saveRegistraUnaSerieNuevaYRecargaLaVersionPersistida() {
        Series enviada = new Series();
        Series persistida = new Series();
        persistida.setId_serie(15L);
        persistida.setNombre_serie("Contratos");
        when(repository.save(enviada)).thenReturn(persistida);
        when(repository.findById(15L)).thenReturn(Optional.of(persistida));
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = seriesView.save(enviada, ra);

        assertThat(destino).isEqualTo("redirect:/view/series");
        assertThat(flash(ra))
                .containsEntry("success", "Serie registrada con exito")
                .containsEntry("savedSeries", persistida);
    }

    @Test
    void saveInformaLaActualizacionCuandoLaSerieYaTeniaId() {
        Series enviada = new Series();
        enviada.setId_serie(4L);
        when(repository.save(enviada)).thenReturn(enviada);
        when(repository.findById(4L)).thenReturn(Optional.empty());
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        seriesView.save(enviada, ra);

        assertThat(flash(ra))
                .containsEntry("success", "Serie actualizada con exito")
                .containsEntry("savedSeries", enviada);
    }

    @Test
    void editCargaLaSerieSolicitada() {
        Series serie = new Series();
        when(repository.findById(9L)).thenReturn(Optional.of(serie));
        when(unidadesRepository.findAll()).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String vista = seriesView.edit(9L, model);

        assertThat(vista).isEqualTo("series/seriesForm");
        assertThat(model.getAttribute("series")).isSameAs(serie);
    }

    @Test
    void editDevuelveUnaSerieVaciaCuandoNoExiste() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        when(unidadesRepository.findAll()).thenReturn(List.of());
        Model model = new ConcurrentModel();

        seriesView.edit(9L, model);

        assertThat(((Series) model.getAttribute("series")).getId_serie()).isNull();
    }

    @Test
    void deleteEliminaLaSerieYRedirigeAlListado() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = seriesView.delete(3L, ra);

        verify(repository).deleteById(3L);
        verify(repository, never()).save(any(Series.class));
        assertThat(destino).isEqualTo("redirect:/view/series");
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Serie eliminada con exito");
    }

    private static Map<String, Object> flash(RedirectAttributesModelMap ra) {
        return new HashMap<>(ra.getFlashAttributes());
    }
}
