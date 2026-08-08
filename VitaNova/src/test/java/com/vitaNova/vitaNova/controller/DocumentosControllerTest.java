package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Documentos;
import com.vitaNova.vitaNova.repository.DocumentosRepository;
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
class DocumentosControllerTest {

    @Mock
    private DocumentosRepository documentosRepository;

    @InjectMocks
    private DocumentosController documentosController;

    @Test
    void getAllDevuelveTodosLosDocumentos() {
        List<Documentos> documentos = List.of(new Documentos());
        when(documentosRepository.findAll()).thenReturn(documentos);

        assertThat(documentosController.getAll()).isEqualTo(documentos);
    }

    @Test
    void getByIdDevuelveElDocumentoSolicitado() {
        Documentos documento = new Documentos();
        when(documentosRepository.findById(1L)).thenReturn(Optional.of(documento));

        assertThat(documentosController.getById(1L)).isSameAs(documento);
    }

    @Test
    void getByIdDevuelveNuloCuandoNoExiste() {
        when(documentosRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(documentosController.getById(1L)).isNull();
    }

    @Test
    void createPersisteElDocumentoRecibido() {
        Documentos documento = new Documentos();
        when(documentosRepository.save(documento)).thenReturn(documento);

        assertThat(documentosController.create(documento)).isSameAs(documento);
    }

    @Test
    void updateAsignaElIdDeLaRutaAntesDeGuardar() {
        Documentos documento = new Documentos();
        when(documentosRepository.save(documento)).thenReturn(documento);

        assertThat(documentosController.update(8L, documento)).isSameAs(documento);
        assertThat(documento.getId_documento()).isEqualTo(8L);
    }

    @Test
    void deleteEliminaElDocumentoPorId() {
        documentosController.delete(8L);

        verify(documentosRepository).deleteById(8L);
    }
}
