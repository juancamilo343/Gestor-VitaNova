package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Reasignaciones;
import com.vitaNova.vitaNova.repository.ReasignacionesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReasignacionesViewTest {

    @Mock
    private ReasignacionesRepository reasignacionesRepository;

    @InjectMocks
    private ReasignacionesView reasignacionesView;

    @Test
    void listaPublicaLosRegistros() {
        List<Reasignaciones> registros = List.of(new Reasignaciones());
        when(reasignacionesRepository.findAll()).thenReturn(registros);
        Model model = new ConcurrentModel();

        String vista = reasignacionesView.lista(model);

        assertThat(vista).isEqualTo("reasignaciones");
        assertThat(model.getAttribute("reasignaciones")).isEqualTo(registros);
    }

    @Test
    void formPreparaUnRegistroVacio() {
        Model model = new ConcurrentModel();

        String vista = reasignacionesView.form(model);

        assertThat(vista).isEqualTo("reasignacionesForm");
        assertThat(model.getAttribute("reasignaciones")).isInstanceOf(Reasignaciones.class);
    }

    @Test
    void saveGuardaElRegistroYRedirigeAlListado() {
        Reasignaciones registro = new Reasignaciones();
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = reasignacionesView.save(registro, ra);

        verify(reasignacionesRepository).save(registro);
        assertThat(destino).isEqualTo("redirect:/view/reasignaciones");
        assertThat(ra.getFlashAttributes().get("mensaje")).isEqualTo("Reasignación registrada con éxito");
    }

    @Test
    void editCargaElRegistroSolicitado() {
        Reasignaciones registro = new Reasignaciones();
        when(reasignacionesRepository.findById(6L)).thenReturn(Optional.of(registro));
        Model model = new ConcurrentModel();

        String vista = reasignacionesView.edit(6L, model);

        assertThat(vista).isEqualTo("reasignacionesForm");
        assertThat(model.getAttribute("reasignaciones")).isSameAs(registro);
    }

    @Test
    void editDejaElRegistroEnNuloCuandoNoExiste() {
        when(reasignacionesRepository.findById(6L)).thenReturn(Optional.empty());
        Model model = new ConcurrentModel();

        reasignacionesView.edit(6L, model);

        assertThat(model.getAttribute("reasignaciones")).isNull();
    }

    @Test
    void deleteEliminaElRegistroYRedirigeAlListado() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = reasignacionesView.delete(6L, ra);

        verify(reasignacionesRepository).deleteById(6L);
        assertThat(destino).isEqualTo("redirect:/view/reasignaciones");
        assertThat(ra.getFlashAttributes().get("mensaje")).isEqualTo("Reasignación eliminada con éxito");
    }
}
