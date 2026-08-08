package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Rol;
import com.vitaNova.vitaNova.model.Usuarios;
import com.vitaNova.vitaNova.repository.RolRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuariosControllerTest {

    @Mock
    private UsuariosRepository usuariosRepository;

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private UsuariosController usuariosController;

    @Test
    void addLayoutAttributesPublicaLosDatosDeLaPlantilla() {
        Model model = new ConcurrentModel();

        usuariosController.addLayoutAttributes(model);

        assertThat(model.asMap())
                .containsEntry("activeMenu", "usuarios")
                .containsEntry("userName", "Administrador")
                .containsEntry("userRole", "Farmacia Central");
    }

    @Test
    void listaMuestraTodosLosUsuariosCuandoNoHayBusqueda() {
        List<Usuarios> usuarios = List.of(new Usuarios());
        when(usuariosRepository.findAll()).thenReturn(usuarios);
        Model model = new ConcurrentModel();

        String vista = usuariosController.lista(null, model);

        assertThat(vista).isEqualTo("view/usuarios");
        assertThat(model.getAttribute("usuarios")).isEqualTo(usuarios);
        assertThat(model.getAttribute("pageTitle")).isEqualTo("Gestión de Usuarios");
        assertThat(model.asMap()).doesNotContainKey("search");
        verify(usuariosRepository, never()).search(any());
    }

    @Test
    void listaFiltraPorElTerminoDeBusquedaSinEspacios() {
        List<Usuarios> encontrados = List.of(new Usuarios());
        when(usuariosRepository.search("camilo")).thenReturn(encontrados);
        Model model = new ConcurrentModel();

        usuariosController.lista("  camilo  ", model);

        assertThat(model.getAttribute("usuarios")).isEqualTo(encontrados);
        assertThat(model.getAttribute("search")).isEqualTo("camilo");
        verify(usuariosRepository, never()).findAll();
    }

    @Test
    void listaIgnoraUnaBusquedaEnBlanco() {
        when(usuariosRepository.findAll()).thenReturn(List.of());
        Model model = new ConcurrentModel();

        usuariosController.lista("   ", model);

        verify(usuariosRepository).findAll();
        assertThat(model.asMap()).doesNotContainKey("search");
    }

    @Test
    void formPreparaUnUsuarioActivoConLosRolesDisponibles() {
        List<Rol> roles = List.of(new Rol());
        when(rolRepository.findAll()).thenReturn(roles);
        Model model = new ConcurrentModel();

        String vista = usuariosController.form(model);

        assertThat(vista).isEqualTo("view/usuarios/form");
        assertThat(((Usuarios) model.getAttribute("usuario")).getEstado()).isTrue();
        assertThat(model.getAttribute("roles")).isEqualTo(roles);
        assertThat(model.getAttribute("editMode")).isEqualTo(false);
    }

    @Test
    void saveRegistraUnUsuarioNuevoYLoActivaPorDefecto() {
        Usuarios usuario = new Usuarios();
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = usuariosController.save(usuario, ra);

        assertThat(destino).isEqualTo("redirect:/view/usuarios");
        assertThat(usuario.getEstado()).isTrue();
        verify(usuariosRepository).save(usuario);
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Usuario registrado con éxito");
    }

    @Test
    void saveInformaLaActualizacionDeUnUsuarioExistente() {
        Usuarios usuario = new Usuarios();
        usuario.setId_usuario(4L);
        usuario.setEstado(false);
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        usuariosController.save(usuario, ra);

        assertThat(usuario.getEstado()).isFalse();
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Usuario actualizado con éxito");
    }

    @Test
    void editCargaElUsuarioEnModoEdicion() {
        Usuarios usuario = new Usuarios();
        when(usuariosRepository.findById(3L)).thenReturn(Optional.of(usuario));
        when(rolRepository.findAll()).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String vista = usuariosController.edit(3L, model, new RedirectAttributesModelMap());

        assertThat(vista).isEqualTo("view/usuarios/form");
        assertThat(model.getAttribute("usuario")).isSameAs(usuario);
        assertThat(model.getAttribute("editMode")).isEqualTo(true);
    }

    @Test
    void editRedirigeAlListadoCuandoElUsuarioNoExiste() {
        when(usuariosRepository.findById(9L)).thenReturn(Optional.empty());
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = usuariosController.edit(9L, new ConcurrentModel(), ra);

        assertThat(destino).isEqualTo("redirect:/view/usuarios");
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Usuario no encontrado");
    }

    @Test
    void deleteEliminaElUsuarioCuandoExiste() {
        when(usuariosRepository.existsById(2L)).thenReturn(true);
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = usuariosController.delete(2L, ra);

        assertThat(destino).isEqualTo("redirect:/view/usuarios");
        verify(usuariosRepository).deleteById(2L);
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Usuario eliminado con éxito");
    }

    @Test
    void deleteNoEliminaNadaCuandoElUsuarioNoExiste() {
        when(usuariosRepository.existsById(8L)).thenReturn(false);
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        usuariosController.delete(8L, ra);

        verify(usuariosRepository, never()).deleteById(8L);
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Usuario no encontrado");
    }
}
