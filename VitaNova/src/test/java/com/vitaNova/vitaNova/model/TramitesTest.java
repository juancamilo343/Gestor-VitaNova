package com.vitaNova.vitaNova.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TramitesTest {

    @Test
    void losAccesoresDeLasRelacionesDeSoloLecturaExponenLosIdsPlanos() {
        Tramites tramite = new Tramites();

        tramite.setIdEstadoInicial(3L);
        tramite.setIdDependenciaResponsable(7L);

        assertThat(tramite.getIdEstadoInicial()).isEqualTo(3L);
        assertThat(tramite.getIdDependenciaResponsable()).isEqualTo(7L);
    }
}
