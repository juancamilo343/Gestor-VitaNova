package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Tramites;
import com.vitaNova.vitaNova.repository.TramitesRepository;
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
class TramitesControllerTest {

    @Mock
    private TramitesRepository tramitesRepository;

    @InjectMocks
    private TramitesController tramitesController;

    @Test
    void getAllDevuelveTodosLosTramites() {
        List<Tramites> tramites = List.of(new Tramites());
        when(tramitesRepository.findAll()).thenReturn(tramites);

        assertThat(tramitesController.getAll()).isEqualTo(tramites);
    }

    @Test
    void getByIdDevuelveElTramiteSolicitado() {
        Tramites tramite = new Tramites();
        when(tramitesRepository.findById(1L)).thenReturn(Optional.of(tramite));

        ResponseEntity<Tramites> respuesta = tramitesController.getById(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isSameAs(tramite);
    }

    @Test
    void getByIdDevuelve404CuandoNoExiste() {
        when(tramitesRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(tramitesController.getById(1L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createPersisteElTramiteRecibido() {
        Tramites tramite = new Tramites();
        when(tramitesRepository.save(tramite)).thenReturn(tramite);

        assertThat(tramitesController.create(tramite)).isSameAs(tramite);
    }

    @Test
    void updateAsignaElIdDeLaRutaCuandoElTramiteExiste() {
        Tramites existente = new Tramites();
        Tramites enviado = new Tramites();
        when(tramitesRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(tramitesRepository.save(enviado)).thenReturn(enviado);

        ResponseEntity<Tramites> respuesta = tramitesController.update(5L, enviado);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(enviado.getIdTramite()).isEqualTo(5L);
    }

    @Test
    void updateDevuelve404YNoGuardaCuandoElTramiteNoExiste() {
        Tramites enviado = new Tramites();
        when(tramitesRepository.findById(5L)).thenReturn(Optional.empty());

        assertThat(tramitesController.update(5L, enviado).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(tramitesRepository, never()).save(enviado);
    }

    @Test
    void deleteDevuelve204CuandoElTramiteExiste() {
        when(tramitesRepository.existsById(3L)).thenReturn(true);

        assertThat(tramitesController.delete(3L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(tramitesRepository).deleteById(3L);
    }

    @Test
    void deleteDevuelve404CuandoElTramiteNoExiste() {
        when(tramitesRepository.existsById(3L)).thenReturn(false);

        assertThat(tramitesController.delete(3L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(tramitesRepository, never()).deleteById(3L);
    }
}
