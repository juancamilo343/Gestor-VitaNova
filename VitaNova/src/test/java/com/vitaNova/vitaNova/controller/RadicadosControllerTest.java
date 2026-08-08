package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Radicados;
import com.vitaNova.vitaNova.repository.RadicadosRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RadicadosControllerTest {

    @Mock
    private RadicadosRepository radicadosRepository;

    @InjectMocks
    private RadicadosController radicadosController;

    @Test
    void getAllDevuelveTodosLosRadicados() {
        List<Radicados> radicados = List.of(new Radicados());
        when(radicadosRepository.findAll()).thenReturn(radicados);

        assertThat(radicadosController.getAll()).isEqualTo(radicados);
    }

    @Test
    void getByIdDevuelveElRadicadoSolicitado() {
        Radicados radicado = new Radicados();
        when(radicadosRepository.findById(2L)).thenReturn(Optional.of(radicado));

        ResponseEntity<Radicados> respuesta = radicadosController.getById(2L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isSameAs(radicado);
    }

    @Test
    void getByIdDevuelve404CuandoNoExiste() {
        when(radicadosRepository.findById(2L)).thenReturn(Optional.empty());

        assertThat(radicadosController.getById(2L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createPersisteElRadicadoRecibido() {
        Radicados radicado = new Radicados();
        when(radicadosRepository.save(radicado)).thenReturn(radicado);

        assertThat(radicadosController.create(radicado)).isSameAs(radicado);
    }

    @Test
    void updateAsignaElIdDeLaRutaCuandoElRadicadoExiste() {
        Radicados enviado = new Radicados();
        when(radicadosRepository.findById(6L)).thenReturn(Optional.of(new Radicados()));
        when(radicadosRepository.save(enviado)).thenReturn(enviado);

        ResponseEntity<Radicados> respuesta = radicadosController.update(6L, enviado);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(enviado.getId_radicado()).isEqualTo(6L);
    }

    @Test
    void updateDevuelve404YNoGuardaCuandoElRadicadoNoExiste() {
        Radicados enviado = new Radicados();
        when(radicadosRepository.findById(6L)).thenReturn(Optional.empty());

        assertThat(radicadosController.update(6L, enviado).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(radicadosRepository, never()).save(enviado);
    }

    @Test
    void deleteDevuelve204CuandoElRadicadoExiste() {
        when(radicadosRepository.existsById(4L)).thenReturn(true);

        assertThat(radicadosController.delete(4L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(radicadosRepository).deleteById(4L);
    }

    @Test
    void deleteDevuelve404CuandoElRadicadoNoExiste() {
        when(radicadosRepository.existsById(4L)).thenReturn(false);

        assertThat(radicadosController.delete(4L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(radicadosRepository, never()).deleteById(4L);
    }
}
