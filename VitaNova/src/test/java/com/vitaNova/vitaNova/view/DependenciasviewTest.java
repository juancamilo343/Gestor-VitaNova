package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Dependencias;
import com.vitaNova.vitaNova.repository.DependenciasRepository;
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
class DependenciasviewTest {

    @Mock
    private DependenciasRepository dependenciasRepository;

    @InjectMocks
    private Dependenciasview dependenciasview;

    @Test
    void listaPublicaLosRegistros() {
        List<Dependencias> registros = List.of(new Dependencias());
        when(dependenciasRepository.findAll()).thenReturn(registros);
        Model model = new ConcurrentModel();

        String vista = dependenciasview.lista(model);

        assertThat(vista).isEqualTo("dependencias/dependencias");
        assertThat(model.getAttribute("dependencias")).isEqualTo(registros);
    }

    @Test
    void formPreparaUnRegistroVacio() {
        Model model = new ConcurrentModel();

        String vista = dependenciasview.form(model);

        assertThat(vista).isEqualTo("dependencias/dependenciasForm");
        assertThat(model.getAttribute("dependencias")).isInstanceOf(Dependencias.class);
    }

    @Test
    void saveGuardaElRegistroYRedirigeAlListado() {
        Dependencias registro = new Dependencias();
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = dependenciasview.save(registro, ra);

        verify(dependenciasRepository).save(registro);
        assertThat(destino).isEqualTo("redirect:/view/dependencias");
        assertThat(ra.getFlashAttributes().get("mensaje")).isEqualTo("Dependencia registrado exitosamente");
    }

    @Test
    void editCargaElRegistroSolicitado() {
        Dependencias registro = new Dependencias();
        when(dependenciasRepository.findById(5L)).thenReturn(Optional.of(registro));
        Model model = new ConcurrentModel();

        String vista = dependenciasview.edit(5L, model);

        assertThat(vista).isEqualTo("dependencias/dependenciasForm");
        assertThat(model.getAttribute("dependencias")).isSameAs(registro);
    }

    @Test
    void editDejaElRegistroEnNuloCuandoNoExiste() {
        when(dependenciasRepository.findById(5L)).thenReturn(Optional.empty());
        Model model = new ConcurrentModel();

        dependenciasview.edit(5L, model);

        assertThat(model.getAttribute("dependencias")).isNull();
    }

    @Test
    void deleteEliminaElRegistroYRedirigeAlListado() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = dependenciasview.delete(5L, ra);

        verify(dependenciasRepository).deleteById(5L);
        assertThat(destino).isEqualTo("redirect:/view/dependencias");
        assertThat(ra.getFlashAttributes().get("mensaje")).isEqualTo("dependencia eliminado exitosamente");
    }
}
