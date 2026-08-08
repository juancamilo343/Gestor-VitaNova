package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Plantilla;
import com.vitaNova.vitaNova.repository.PlantillaRepository;
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
class PlantillaViewTest {

    @Mock
    private PlantillaRepository plantillaRepository;

    @InjectMocks
    private PlantillaView plantillaView;

    @Test
    void listaPublicaLosRegistros() {
        List<Plantilla> registros = List.of(new Plantilla());
        when(plantillaRepository.findAll()).thenReturn(registros);
        Model model = new ConcurrentModel();

        String vista = plantillaView.lista(model);

        assertThat(vista).isEqualTo("Plantilla/Plantilla");
        assertThat(model.getAttribute("plantilla")).isEqualTo(registros);
    }

    @Test
    void formPreparaUnRegistroVacio() {
        Model model = new ConcurrentModel();

        String vista = plantillaView.form(model);

        assertThat(vista).isEqualTo("Plantilla/PlantillaForm");
        assertThat(model.getAttribute("plantilla")).isInstanceOf(Plantilla.class);
    }

    @Test
    void saveGuardaElRegistroYRedirigeAlListado() {
        Plantilla registro = new Plantilla();
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = plantillaView.save(registro, ra);

        verify(plantillaRepository).save(registro);
        assertThat(destino).isEqualTo("redirect:/view/bitacoras");
        assertThat(ra.getFlashAttributes().get("mensaje")).isEqualTo("Plantilla registrada con éxito");
    }

    @Test
    void editCargaElRegistroSolicitado() {
        Plantilla registro = new Plantilla();
        when(plantillaRepository.findById(7L)).thenReturn(Optional.of(registro));
        Model model = new ConcurrentModel();

        String vista = plantillaView.edit(7L, model);

        assertThat(vista).isEqualTo("Plantilla/BitacorasForm");
        assertThat(model.getAttribute("bitacora")).isSameAs(registro);
    }

    @Test
    void editDejaElRegistroEnNuloCuandoNoExiste() {
        when(plantillaRepository.findById(7L)).thenReturn(Optional.empty());
        Model model = new ConcurrentModel();

        plantillaView.edit(7L, model);

        assertThat(model.getAttribute("bitacora")).isNull();
    }

    @Test
    void deleteEliminaElRegistroYRedirigeAlListado() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = plantillaView.delete(7L, ra);

        verify(plantillaRepository).deleteById(7L);
        assertThat(destino).isEqualTo("redirect:/view/bitacoras");
        assertThat(ra.getFlashAttributes().get("mensaje")).isEqualTo("Bitácora eliminada con éxito");
    }
}
