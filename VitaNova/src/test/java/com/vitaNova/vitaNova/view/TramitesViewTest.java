package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Tramites;
import com.vitaNova.vitaNova.repository.DependenciasRepository;
import com.vitaNova.vitaNova.repository.EstadosRepository;
import com.vitaNova.vitaNova.repository.RadicadosRepository;
import com.vitaNova.vitaNova.repository.TramitesRepository;
import com.vitaNova.vitaNova.repository.UsuariosRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TramitesViewTest {

    @Mock
    private TramitesRepository tramitesRepository;

    @Mock
    private RadicadosRepository radicadosRepository;

    @Mock
    private EstadosRepository estadosRepository;

    @Mock
    private DependenciasRepository dependenciasRepository;

    @Mock
    private UsuariosRepository usuariosRepository;

    @InjectMocks
    private TramitesView tramitesView;

    @Test
    void listaPublicaLosCatalogosYLosIndicadores() {
        when(tramitesRepository.count()).thenReturn(6L);
        when(radicadosRepository.countPendientes()).thenReturn(3L);
        when(radicadosRepository.countEnTramite()).thenReturn(2L);
        when(radicadosRepository.countFinalizados()).thenReturn(1L);
        when(radicadosRepository.countVencidos()).thenReturn(0L);
        Model model = new ConcurrentModel();

        String vista = tramitesView.lista(model);

        assertThat(vista).isEqualTo("tramites/tramites");
        assertThat(model.asMap())
                .containsKeys("radicados", "tramites", "estados", "dependencias", "usuarios")
                .containsEntry("totalTramites", 6L)
                .containsEntry("pendientes", 3L)
                .containsEntry("enProceso", 2L)
                .containsEntry("finalizados", 1L)
                .containsEntry("vencidos", 0L);
    }

    @Test
    void formPreparaUnTramiteVacioConSusCatalogos() {
        when(estadosRepository.findAll()).thenReturn(List.of());
        when(dependenciasRepository.findAll()).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String vista = tramitesView.form(model);

        assertThat(vista).isEqualTo("tramites/tramitesForm");
        assertThat(model.getAttribute("tramites")).isInstanceOf(Tramites.class);
    }

    @Test
    void editCargaElTramiteSolicitado() {
        Tramites tramite = new Tramites();
        when(tramitesRepository.findById(4L)).thenReturn(Optional.of(tramite));
        Model model = new ConcurrentModel();

        String vista = tramitesView.edit(4L, model);

        assertThat(vista).isEqualTo("tramites/tramitesForm");
        assertThat(model.getAttribute("tramites")).isSameAs(tramite);
    }

    @Test
    void editFallaCuandoElTramiteNoExiste() {
        when(tramitesRepository.findById(77L)).thenReturn(Optional.empty());
        Model model = new ConcurrentModel();

        assertThatThrownBy(() -> tramitesView.edit(77L, model))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Trámite no encontrado");
    }

    @Test
    void saveGuardaElTramiteYRedirigeAlListado() {
        Tramites tramite = new Tramites();
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = tramitesView.save(tramite, ra);

        verify(tramitesRepository).save(tramite);
        assertThat(destino).isEqualTo("redirect:/view/tramites");
        assertThat(ra.getFlashAttributes().get("mensaje")).isEqualTo("Trámite registrado con éxito");
    }

    @Test
    void deleteEliminaElTramiteYRedirigeAlListado() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = tramitesView.delete(2L, ra);

        verify(tramitesRepository).deleteById(2L);
        assertThat(destino).isEqualTo("redirect:/view/tramites");
        assertThat(ra.getFlashAttributes().get("mensaje")).isEqualTo("Trámite eliminado con éxito");
    }
}
