package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Dependencias;
import com.vitaNova.vitaNova.repository.DependenciasRepository;
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
class DependenciasControllerTest {

    @Mock
    private DependenciasRepository dependenciasRepository;

    @InjectMocks
    private DependenciasController dependenciasController;

    @Test
    void getAllDevuelveTodosLosRegistros() {
        List<Dependencias> registros = List.of(new Dependencias());
        when(dependenciasRepository.findAll()).thenReturn(registros);

        assertThat(dependenciasController.getAll()).isEqualTo(registros);
    }

    @Test
    void getByIdDevuelveElRegistroSolicitado() {
        Dependencias registro = new Dependencias();
        when(dependenciasRepository.findById(4L)).thenReturn(Optional.of(registro));

        assertThat(dependenciasController.getById(4L)).isSameAs(registro);
    }

    @Test
    void getByIdDevuelveNuloCuandoNoExiste() {
        when(dependenciasRepository.findById(4L)).thenReturn(Optional.empty());

        assertThat(dependenciasController.getById(4L)).isNull();
    }

    @Test
    void createPersisteElRegistroRecibido() {
        Dependencias registro = new Dependencias();
        when(dependenciasRepository.save(registro)).thenReturn(registro);

        assertThat(dependenciasController.create(registro)).isSameAs(registro);
    }

    @Test
    void updateAsignaElIdDeLaRutaAntesDeGuardar() {
        Dependencias registro = new Dependencias();
        when(dependenciasRepository.save(registro)).thenReturn(registro);

        assertThat(dependenciasController.update(4L, registro)).isSameAs(registro);
        assertThat(registro.getId_dependencia()).isEqualTo(4L);
    }

    @Test
    void deleteEliminaElRegistroPorId() {
        dependenciasController.delete(4L);

        verify(dependenciasRepository).deleteById(4L);
    }
}
