package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Plantilla;
import com.vitaNova.vitaNova.repository.PlantillaRepository;
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
class PlantillaControllerTest {

    @Mock
    private PlantillaRepository plantillaRepository;

    @InjectMocks
    private PlantillaController plantillaController;

    @Test
    void getAllDevuelveTodosLosRegistros() {
        List<Plantilla> registros = List.of(new Plantilla());
        when(plantillaRepository.findAll()).thenReturn(registros);

        assertThat(plantillaController.getAll()).isEqualTo(registros);
    }

    @Test
    void getByIdDevuelveElRegistroSolicitado() {
        Plantilla registro = new Plantilla();
        when(plantillaRepository.findById(6L)).thenReturn(Optional.of(registro));

        assertThat(plantillaController.getById(6L)).isSameAs(registro);
    }

    @Test
    void getByIdDevuelveNuloCuandoNoExiste() {
        when(plantillaRepository.findById(6L)).thenReturn(Optional.empty());

        assertThat(plantillaController.getById(6L)).isNull();
    }

    @Test
    void createPersisteElRegistroRecibido() {
        Plantilla registro = new Plantilla();
        when(plantillaRepository.save(registro)).thenReturn(registro);

        assertThat(plantillaController.create(registro)).isSameAs(registro);
    }

    @Test
    void updateAsignaElIdDeLaRutaAntesDeGuardar() {
        Plantilla registro = new Plantilla();
        when(plantillaRepository.save(registro)).thenReturn(registro);

        assertThat(plantillaController.update(6L, registro)).isSameAs(registro);
        assertThat(registro.getId_evento()).isEqualTo(6L);
    }

    @Test
    void deleteEliminaElRegistroPorId() {
        plantillaController.delete(6L);

        verify(plantillaRepository).deleteById(6L);
    }
}
