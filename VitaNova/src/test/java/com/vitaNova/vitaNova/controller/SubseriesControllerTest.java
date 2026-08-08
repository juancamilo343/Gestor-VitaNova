package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Subseries;
import com.vitaNova.vitaNova.repository.SubseriesRepository;
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
class SubseriesControllerTest {

    @Mock
    private SubseriesRepository repository;

    @InjectMocks
    private SubseriesController subseriesController;

    @Test
    void getAllDevuelveTodosLosRegistros() {
        List<Subseries> registros = List.of(new Subseries());
        when(repository.findAll()).thenReturn(registros);

        assertThat(subseriesController.getAll()).isEqualTo(registros);
    }

    @Test
    void getByIdDevuelveElRegistroSolicitado() {
        Subseries registro = new Subseries();
        when(repository.findById(2L)).thenReturn(Optional.of(registro));

        assertThat(subseriesController.getById(2L)).isSameAs(registro);
    }

    @Test
    void getByIdDevuelveNuloCuandoNoExiste() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThat(subseriesController.getById(2L)).isNull();
    }

    @Test
    void createPersisteElRegistroRecibido() {
        Subseries registro = new Subseries();
        when(repository.save(registro)).thenReturn(registro);

        assertThat(subseriesController.create(registro)).isSameAs(registro);
    }

    @Test
    void updateAsignaElIdDeLaRutaAntesDeGuardar() {
        Subseries registro = new Subseries();
        when(repository.save(registro)).thenReturn(registro);

        assertThat(subseriesController.update(2L, registro)).isSameAs(registro);
        assertThat(registro.getId_subserie()).isEqualTo(2L);
    }

    @Test
    void deleteEliminaElRegistroPorId() {
        subseriesController.delete(2L);

        verify(repository).deleteById(2L);
    }
}
