package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Reasignaciones;
import com.vitaNova.vitaNova.repository.ReasignacionesRepository;
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
class ReasignacionesControllerTest {

    @Mock
    private ReasignacionesRepository reasignacionesRepository;

    @InjectMocks
    private ReasignacionesController reasignacionesController;

    @Test
    void getAllDevuelveTodosLosRegistros() {
        List<Reasignaciones> registros = List.of(new Reasignaciones());
        when(reasignacionesRepository.findAll()).thenReturn(registros);

        assertThat(reasignacionesController.getAll()).isEqualTo(registros);
    }

    @Test
    void getByIdDevuelveElRegistroSolicitado() {
        Reasignaciones registro = new Reasignaciones();
        when(reasignacionesRepository.findById(7L)).thenReturn(Optional.of(registro));

        assertThat(reasignacionesController.getById(7L)).isSameAs(registro);
    }

    @Test
    void getByIdDevuelveNuloCuandoNoExiste() {
        when(reasignacionesRepository.findById(7L)).thenReturn(Optional.empty());

        assertThat(reasignacionesController.getById(7L)).isNull();
    }

    @Test
    void createPersisteElRegistroRecibido() {
        Reasignaciones registro = new Reasignaciones();
        when(reasignacionesRepository.save(registro)).thenReturn(registro);

        assertThat(reasignacionesController.create(registro)).isSameAs(registro);
    }

    @Test
    void updateAsignaElIdDeLaRutaAntesDeGuardar() {
        Reasignaciones registro = new Reasignaciones();
        when(reasignacionesRepository.save(registro)).thenReturn(registro);

        assertThat(reasignacionesController.update(7L, registro)).isSameAs(registro);
        assertThat(registro.getId_reasignacion()).isEqualTo(7L);
    }

    @Test
    void deleteEliminaElRegistroPorId() {
        reasignacionesController.delete(7L);

        verify(reasignacionesRepository).deleteById(7L);
    }
}
