package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Estados;
import com.vitaNova.vitaNova.repository.EstadosRepository;
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
class EstadosControllerTest {

    @Mock
    private EstadosRepository estadosRepository;

    @InjectMocks
    private EstadosController estadosController;

    @Test
    void getAllDevuelveTodosLosRegistros() {
        List<Estados> registros = List.of(new Estados());
        when(estadosRepository.findAll()).thenReturn(registros);

        assertThat(estadosController.getAll()).isEqualTo(registros);
    }

    @Test
    void getByIdDevuelveElRegistroSolicitado() {
        Estados registro = new Estados();
        when(estadosRepository.findById(3L)).thenReturn(Optional.of(registro));

        assertThat(estadosController.getById(3L)).isSameAs(registro);
    }

    @Test
    void getByIdDevuelveNuloCuandoNoExiste() {
        when(estadosRepository.findById(3L)).thenReturn(Optional.empty());

        assertThat(estadosController.getById(3L)).isNull();
    }

    @Test
    void createPersisteElRegistroRecibido() {
        Estados registro = new Estados();
        when(estadosRepository.save(registro)).thenReturn(registro);

        assertThat(estadosController.create(registro)).isSameAs(registro);
    }

    @Test
    void updateAsignaElIdDeLaRutaAntesDeGuardar() {
        Estados registro = new Estados();
        when(estadosRepository.save(registro)).thenReturn(registro);

        assertThat(estadosController.update(3L, registro)).isSameAs(registro);
        assertThat(registro.getId_estado()).isEqualTo(3L);
    }

    @Test
    void deleteEliminaElRegistroPorId() {
        estadosController.delete(3L);

        verify(estadosRepository).deleteById(3L);
    }
}
