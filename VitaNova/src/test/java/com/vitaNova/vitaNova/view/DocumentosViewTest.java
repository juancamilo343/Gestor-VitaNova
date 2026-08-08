package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Documentos;
import com.vitaNova.vitaNova.repository.DocumentosRepository;
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
class DocumentosViewTest {

    @Mock
    private DocumentosRepository documentosRepository;

    @InjectMocks
    private DocumentosView documentosView;

    @Test
    void listaPublicaLosRegistros() {
        List<Documentos> registros = List.of(new Documentos());
        when(documentosRepository.findAll()).thenReturn(registros);
        Model model = new ConcurrentModel();

        String vista = documentosView.lista(model);

        assertThat(vista).isEqualTo("documentos/documentos");
        assertThat(model.getAttribute("documentos")).isEqualTo(registros);
    }

    @Test
    void formPreparaUnRegistroVacio() {
        Model model = new ConcurrentModel();

        String vista = documentosView.form(model);

        assertThat(vista).isEqualTo("documentos/documentosForm");
        assertThat(model.getAttribute("documentos")).isInstanceOf(Documentos.class);
    }

    @Test
    void saveGuardaElRegistroYRedirigeAlListado() {
        Documentos registro = new Documentos();
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = documentosView.save(registro, ra);

        verify(documentosRepository).save(registro);
        assertThat(destino).isEqualTo("redirect:/view/documentos");
        assertThat(ra.getFlashAttributes().get("mensaje")).isEqualTo("documentos registrado exitosamente");
    }

    @Test
    void editCargaElRegistroSolicitado() {
        Documentos registro = new Documentos();
        when(documentosRepository.findById(3L)).thenReturn(Optional.of(registro));
        Model model = new ConcurrentModel();

        String vista = documentosView.edit(3L, model);

        assertThat(vista).isEqualTo("documentos/documentosForm");
        assertThat(model.getAttribute("documentos")).isSameAs(registro);
    }

    @Test
    void editDejaElRegistroEnNuloCuandoNoExiste() {
        when(documentosRepository.findById(3L)).thenReturn(Optional.empty());
        Model model = new ConcurrentModel();

        documentosView.edit(3L, model);

        assertThat(model.getAttribute("documentos")).isNull();
    }

    @Test
    void deleteEliminaElRegistroYRedirigeAlListado() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = documentosView.delete(3L, ra);

        verify(documentosRepository).deleteById(3L);
        assertThat(destino).isEqualTo("redirect:/view/documentos");
        assertThat(ra.getFlashAttributes().get("mensaje")).isEqualTo("Documentos eliminado exitosamente");
    }
}
