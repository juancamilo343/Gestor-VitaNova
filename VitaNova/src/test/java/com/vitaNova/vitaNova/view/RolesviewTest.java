package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Roles;
import com.vitaNova.vitaNova.repository.RolesRepository;
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
class RolesviewTest {

    @Mock
    private RolesRepository rolesRepository;

    @InjectMocks
    private Rolesview rolesview;

    @Test
    void listaPublicaLosRegistros() {
        List<Roles> registros = List.of(new Roles());
        when(rolesRepository.findAll()).thenReturn(registros);
        Model model = new ConcurrentModel();

        String vista = rolesview.lista(model);

        assertThat(vista).isEqualTo("roles/roles");
        assertThat(model.getAttribute("roles")).isEqualTo(registros);
    }

    @Test
    void formPreparaUnRegistroVacio() {
        Model model = new ConcurrentModel();

        String vista = rolesview.form(model);

        assertThat(vista).isEqualTo("roles/rolesForm");
        assertThat(model.getAttribute("roles")).isInstanceOf(Roles.class);
    }

    @Test
    void saveGuardaElRegistroYRedirigeAlListado() {
        Roles registro = new Roles();
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = rolesview.save(registro, ra);

        verify(rolesRepository).save(registro);
        assertThat(destino).isEqualTo("redirect:/view/roles");
        assertThat(ra.getFlashAttributes().get("mensaje")).isEqualTo("rol registrado exitosamente");
    }

    @Test
    void editCargaElRegistroSolicitado() {
        Roles registro = new Roles();
        when(rolesRepository.findById(4L)).thenReturn(Optional.of(registro));
        Model model = new ConcurrentModel();

        String vista = rolesview.edit(4L, model);

        assertThat(vista).isEqualTo("roles/rolesForm");
        assertThat(model.getAttribute("roles")).isSameAs(registro);
    }

    @Test
    void editDejaElRegistroEnNuloCuandoNoExiste() {
        when(rolesRepository.findById(4L)).thenReturn(Optional.empty());
        Model model = new ConcurrentModel();

        rolesview.edit(4L, model);

        assertThat(model.getAttribute("roles")).isNull();
    }

    @Test
    void deleteEliminaElRegistroYRedirigeAlListado() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = rolesview.delete(4L, ra);

        verify(rolesRepository).deleteById(4L);
        assertThat(destino).isEqualTo("redirect:/view/roles");
        assertThat(ra.getFlashAttributes().get("mensaje")).isEqualTo("rol eliminado exitosamente");
    }
}
