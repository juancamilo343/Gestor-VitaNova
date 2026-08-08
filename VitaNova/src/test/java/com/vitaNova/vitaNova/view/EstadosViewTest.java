package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Estados;
import com.vitaNova.vitaNova.repository.EstadosRepository;
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
class EstadosViewTest {

    @Mock
    private EstadosRepository estadosRepository;

    @InjectMocks
    private EstadosView estadosView;

    @Test
    void listaPublicaLosRegistros() {
        List<Estados> registros = List.of(new Estados());
        when(estadosRepository.findAll()).thenReturn(registros);
        Model model = new ConcurrentModel();

        String vista = estadosView.lista(model);

        assertThat(vista).isEqualTo("estados/estados");
        assertThat(model.getAttribute("estados")).isEqualTo(registros);
    }

    @Test
    void formPreparaUnRegistroVacio() {
        Model model = new ConcurrentModel();

        String vista = estadosView.form(model);

        assertThat(vista).isEqualTo("estados/estadosForm");
        assertThat(model.getAttribute("estados")).isInstanceOf(Estados.class);
    }

    @Test
    void saveGuardaElRegistroYRedirigeAlListado() {
        Estados registro = new Estados();
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = estadosView.save(registro, ra);

        verify(estadosRepository).save(registro);
        assertThat(destino).isEqualTo("redirect:/view/estados");
        assertThat(ra.getFlashAttributes().get("mensaje")).isEqualTo("Estado registrado con exito");
    }

    @Test
    void editCargaElRegistroSolicitado() {
        Estados registro = new Estados();
        when(estadosRepository.findById(2L)).thenReturn(Optional.of(registro));
        Model model = new ConcurrentModel();

        String vista = estadosView.edit(2L, model);

        assertThat(vista).isEqualTo("estadosForm");
        assertThat(model.getAttribute("estados")).isSameAs(registro);
    }

    @Test
    void editDejaElRegistroEnNuloCuandoNoExiste() {
        when(estadosRepository.findById(2L)).thenReturn(Optional.empty());
        Model model = new ConcurrentModel();

        estadosView.edit(2L, model);

        assertThat(model.getAttribute("estados")).isNull();
    }

    @Test
    void deleteEliminaElRegistroYRedirigeAlListado() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = estadosView.delete(2L, ra);

        verify(estadosRepository).deleteById(2L);
        assertThat(destino).isEqualTo("redirect:/view/estados");
        assertThat(ra.getFlashAttributes().get("mensaje")).isEqualTo("Estado eliminado con exito");
    }
}
