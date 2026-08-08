package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Series;
import com.vitaNova.vitaNova.repository.SeriesRepository;
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
class SeriesControllerTest {

    @Mock
    private SeriesRepository repository;

    @InjectMocks
    private SeriesController seriesController;

    @Test
    void getAllDevuelveTodosLosRegistros() {
        List<Series> registros = List.of(new Series());
        when(repository.findAll()).thenReturn(registros);

        assertThat(seriesController.getAll()).isEqualTo(registros);
    }

    @Test
    void getByIdDevuelveElRegistroSolicitado() {
        Series registro = new Series();
        when(repository.findById(1L)).thenReturn(Optional.of(registro));

        assertThat(seriesController.getById(1L)).isSameAs(registro);
    }

    @Test
    void getByIdDevuelveNuloCuandoNoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThat(seriesController.getById(1L)).isNull();
    }

    @Test
    void createPersisteElRegistroRecibido() {
        Series registro = new Series();
        when(repository.save(registro)).thenReturn(registro);

        assertThat(seriesController.create(registro)).isSameAs(registro);
    }

    @Test
    void updateAsignaElIdDeLaRutaAntesDeGuardar() {
        Series registro = new Series();
        when(repository.save(registro)).thenReturn(registro);

        assertThat(seriesController.update(1L, registro)).isSameAs(registro);
        assertThat(registro.getId_serie()).isEqualTo(1L);
    }

    @Test
    void deleteEliminaElRegistroPorId() {
        seriesController.delete(1L);

        verify(repository).deleteById(1L);
    }
}
