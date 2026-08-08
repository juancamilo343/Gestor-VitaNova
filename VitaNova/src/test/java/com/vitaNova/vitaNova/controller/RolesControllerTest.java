package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Roles;
import com.vitaNova.vitaNova.repository.RolesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolesControllerTest {

    @Mock
    private RolesRepository rolesRepository;

    @InjectMocks
    private RolesController rolesController;

    @Test
    void getAllDevuelveTodosLosRegistros() {
        List<Roles> registros = List.of(new Roles());
        when(rolesRepository.findAll()).thenReturn(registros);

        assertThat(rolesController.getAll()).isEqualTo(registros);
    }

    @Test
    void getByIdDevuelveElRegistroSolicitado() {
        Roles registro = new Roles();
        when(rolesRepository.findById(5L)).thenReturn(Optional.of(registro));

        assertThat(rolesController.getById(5L)).isSameAs(registro);
    }

    @Test
    void getByIdDevuelveNuloCuandoNoExiste() {
        when(rolesRepository.findById(5L)).thenReturn(Optional.empty());

        assertThat(rolesController.getById(5L)).isNull();
    }

    @Test
    void createPersisteElRegistroRecibido() {
        Roles registro = new Roles();
        when(rolesRepository.save(registro)).thenReturn(registro);

        assertThat(rolesController.create(registro)).isSameAs(registro);
    }

    @Test
    void updateAsignaElIdDeLaRutaAntesDeGuardar() {
        Roles registro = new Roles();
        when(rolesRepository.save(registro)).thenReturn(registro);

        assertThat(rolesController.update(5L, registro)).isSameAs(registro);
        assertThat(registro.getId_rol()).isEqualTo(5L);
    }

    @Test
    void deleteEliminaElRegistroPorId() {
        rolesController.delete(5L);

        verify(rolesRepository).deleteById(5L);
    }
}
