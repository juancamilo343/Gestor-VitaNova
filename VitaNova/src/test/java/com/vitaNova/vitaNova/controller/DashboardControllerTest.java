package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.dto.DashboardResumen;
import com.vitaNova.vitaNova.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    void dashboardPublicaElResumenYLosDatosDeLaPlantilla() {
        DashboardResumen resumen = new DashboardResumen();
        when(dashboardService.obtenerResumen()).thenReturn(resumen);
        Model model = new ConcurrentModel();

        String vista = dashboardController.dashboard(model);

        String fechaEsperada = LocalDate.now().format(
                DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "CO")));
        assertThat(vista).isEqualTo("dashboard/Dashboard");
        assertThat(model.asMap())
                .containsEntry("dashboard", resumen)
                .containsEntry("activeMenu", "dashboard")
                .containsEntry("pageTitle", "Dashboard")
                .containsEntry("pageSubtitle", "Resumen operativo para " + fechaEsperada)
                .containsEntry("userName", "Administrador")
                .containsEntry("userRole", "Farmacia Central");
    }
}
