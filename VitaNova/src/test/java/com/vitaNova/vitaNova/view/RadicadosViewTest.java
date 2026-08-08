package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Dependencias;
import com.vitaNova.vitaNova.model.Documentos;
import com.vitaNova.vitaNova.model.Radicados;
import com.vitaNova.vitaNova.repository.DependenciasRepository;
import com.vitaNova.vitaNova.repository.DocumentosRepository;
import com.vitaNova.vitaNova.repository.EstadosRepository;
import com.vitaNova.vitaNova.repository.RadicadosRepository;
import com.vitaNova.vitaNova.repository.SeriesRepository;
import com.vitaNova.vitaNova.repository.SubseriesRepository;
import com.vitaNova.vitaNova.repository.TramitesRepository;
import com.vitaNova.vitaNova.repository.UsuariosRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RadicadosViewTest {

    private static final String VISTA_RADICACION = "radicados/radicacion_Documental";
    private static final String URI_SAVE = "/view/radicados/save";

    @Mock
    private RadicadosRepository radicadosRepository;

    @Mock
    private DependenciasRepository dependenciasRepository;

    @Mock
    private SeriesRepository seriesRepository;

    @Mock
    private SubseriesRepository subseriesRepository;

    @Mock
    private TramitesRepository tramitesRepository;

    @Mock
    private EstadosRepository estadosRepository;

    @Mock
    private UsuariosRepository usuariosRepository;

    @Mock
    private DocumentosRepository documentosRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private RadicadosView radicadosView;

    @TempDir
    Path uploadDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(radicadosView, "uploadDir", uploadDir.toString());
        when(request.getRequestURI()).thenReturn(URI_SAVE);
        when(radicadosRepository.save(any(Radicados.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(documentosRepository.save(any(Documentos.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(dependenciasRepository.findById(anyLong())).thenReturn(Optional.empty());
    }

    @Test
    void inicioCargaLosCatalogosDeLaPantallaDeRadicacion() {
        Model model = new ConcurrentModel();

        String vista = radicadosView.inicio(model);

        assertThat(vista).isEqualTo(VISTA_RADICACION);
        assertThat(model.asMap()).containsKeys(
                "radicado", "dependencias", "series", "subseries", "tramites", "estados", "usuarios");
    }

    @Test
    void documentalDevuelveLaMismaPantallaQueInicio() {
        Model model = new ConcurrentModel();

        assertThat(radicadosView.documental(model)).isEqualTo(VISTA_RADICACION);
        assertThat(model.getAttribute("radicado")).isInstanceOf(Radicados.class);
    }

    @Test
    void editCargaElRadicadoExistente() {
        Radicados existente = new Radicados();
        existente.setId_radicado(7L);
        when(radicadosRepository.findById(7L)).thenReturn(Optional.of(existente));
        Model model = new ConcurrentModel();

        String vista = radicadosView.edit(7L, model);

        assertThat(vista).isEqualTo(VISTA_RADICACION);
        assertThat(model.getAttribute("radicado")).isSameAs(existente);
    }

    @Test
    void editDevuelveUnRadicadoVacioCuandoNoExiste() {
        when(radicadosRepository.findById(99L)).thenReturn(Optional.empty());
        Model model = new ConcurrentModel();

        radicadosView.edit(99L, model);

        Radicados radicado = (Radicados) model.getAttribute("radicado");
        assertThat(radicado).isNotNull();
        assertThat(radicado.getId_radicado()).isNull();
    }

    @Test
    void deleteEliminaYRedirigeALaRadicacionDocumental() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = radicadosView.delete(5L, ra);

        verify(radicadosRepository).deleteById(5L);
        assertThat(destino).isEqualTo("redirect:/view/radicados/documental");
        assertThat(flash(ra))
                .containsEntry("success", "Radicacion eliminada con exito")
                .containsEntry("mensaje", "Radicacion eliminada con exito");
    }

    @Test
    void saveGeneraConsecutivoYFechaCuandoElFormularioNoLosEnvia() {
        Radicados radicado = nuevoRadicado();
        radicado.setFechaDocumento("2026-01-15");
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String destino = radicadosView.save(radicado, ra, request);

        assertThat(destino).isEqualTo("redirect:/view/radicados/documental");
        assertThat(radicado.getNumero_radicado()).matches("RAD-\\d{14}");
        assertThat(radicado.getFecha_radicado()).isEqualTo("2026-01-15");
        assertThat(flash(ra))
                .containsEntry("success", "Radicacion documental guardada con exito")
                .containsEntry("archivos", 0);
    }

    @Test
    void saveAplicaLosValoresPorDefectoCuandoLosCamposDelFormularioNoSonNumericos() {
        Radicados radicado = nuevoRadicado();
        radicado.setTipoPQRS("");
        radicado.setTipoDocumento("no-numerico");
        radicado.setCanalRecepcion(null);
        radicado.setResponsable("  ");
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        radicadosView.save(radicado, ra, request);

        assertThat(radicado.getId_tramite()).isEqualTo(1);
        assertThat(radicado.getId_estado()).isEqualTo(1);
        assertThat(radicado.getId_usuario()).isEqualTo(2);
        assertThat(radicado.getRemitente()).isEqualTo("Anónimo");
        assertThat(radicado.getAsunto()).isEqualTo("Sin asunto");
        verify(dependenciasRepository).findById(99L);
    }

    @Test
    void saveUsaLosIdsEnviadosPorElFormularioSegunSuPrioridad() {
        Radicados radicado = nuevoRadicado();
        radicado.setTipoPQRS("4");
        radicado.setTipoDocumento("8");
        radicado.setCanalRecepcion("3");
        radicado.setDependencia("");
        radicado.setDependenciaDestino("12");
        radicado.setDependenciaOrigen("20");
        radicado.setResponsable("6");
        radicado.setObservaciones("Solicitud de certificado");
        Dependencias dependencia = new Dependencias();
        dependencia.setId_dependencia(12L);
        when(dependenciasRepository.findById(12L)).thenReturn(Optional.of(dependencia));
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        radicadosView.save(radicado, ra, request);

        assertThat(radicado.getId_tramite()).isEqualTo(4);
        assertThat(radicado.getId_estado()).isEqualTo(3);
        assertThat(radicado.getId_usuario()).isEqualTo(6);
        assertThat(radicado.getDependencias()).isSameAs(dependencia);
        assertThat(radicado.getRemitente()).isEqualTo("6");
        assertThat(radicado.getAsunto()).isEqualTo("Solicitud de certificado");
    }

    @Test
    void saveNormalizaLaClasificacionDocumentalVaciaANull() {
        Radicados radicado = nuevoRadicado();
        radicado.setCodigo_serie("");
        radicado.setCodigo_subserie("   ");
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        radicadosView.save(radicado, ra, request);

        assertThat(radicado.getCodigo_serie()).isNull();
        assertThat(radicado.getCodigo_subserie()).isNull();
    }

    @Test
    void saveConservaLosDatosYaRadicadosEInformaLaActualizacion() {
        Radicados radicado = nuevoRadicado();
        radicado.setId_radicado(3L);
        radicado.setNumero_radicado("RAD-2026-0001");
        radicado.setFecha_radicado("2026-02-02");
        radicado.setId_tramite(9);
        radicado.setId_estado(5);
        radicado.setId_usuario(4);
        radicado.setRemitente("Juan Camilo");
        radicado.setAsunto("Reclamo");
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        radicadosView.save(radicado, ra, request);

        assertThat(radicado.getNumero_radicado()).isEqualTo("RAD-2026-0001");
        assertThat(radicado.getFecha_radicado()).isEqualTo("2026-02-02");
        assertThat(radicado.getId_tramite()).isEqualTo(9);
        assertThat(flash(ra))
                .containsEntry("success", "Radicacion documental actualizada con exito");
    }

    @Test
    void saveGuardaLosArchivosAdjuntosYRegistraSusMetadatos() {
        Radicados radicado = nuevoRadicado();
        radicado.setId_radicado(11L);
        radicado.setArchivos(new MultipartFile[]{
                new MockMultipartFile("archivos", "oficio.pdf", "application/pdf", "contenido".getBytes()),
                new MockMultipartFile("archivos", "anexo.docx", null, "otro".getBytes()),
                new MockMultipartFile("archivos", "vacio.pdf", "application/pdf", new byte[0])
        });
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        radicadosView.save(radicado, ra, request);

        assertThat(flash(ra)).containsEntry("archivos", 2);

        ArgumentCaptor<Documentos> captor = ArgumentCaptor.forClass(Documentos.class);
        verify(documentosRepository, times(2)).save(captor.capture());
        List<Documentos> documentos = captor.getAllValues();

        assertThat(documentos.get(0).getNombre()).isEqualTo("oficio.pdf");
        assertThat(documentos.get(0).getTipo()).isEqualTo("application/pdf");
        assertThat(documentos.get(0).getId_radicado()).isEqualTo(11L);
        assertThat(documentos.get(0).getTamano()).isEqualTo("contenido".getBytes().length);
        assertThat(documentos.get(0).getNombre_archivo()).startsWith("11_").endsWith("_oficio.pdf");
        assertThat(documentos.get(0).getFecha_subida()).isNotBlank();
        assertThat(Path.of(documentos.get(0).getRuta_archivo())).exists();

        assertThat(documentos.get(1).getTipo()).isEqualTo("docx");
        assertThat(Files.exists(uploadDir.resolve(documentos.get(1).getNombre_archivo()))).isTrue();
    }

    @Test
    void saveNoRegistraDocumentosCuandoNoHayAdjuntos() {
        Radicados radicado = nuevoRadicado();
        radicado.setArchivos(new MultipartFile[0]);
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        radicadosView.save(radicado, ra, request);

        verify(documentosRepository, never()).save(any(Documentos.class));
        assertThat(flash(ra)).containsEntry("archivos", 0);
    }

    @Test
    void saveClasificaComoDesconocidoUnArchivoSinTipoNiExtension() {
        Radicados radicado = nuevoRadicado();
        radicado.setId_radicado(1L);
        radicado.setArchivos(new MultipartFile[]{
                new MockMultipartFile("archivos", "documento", null, "datos".getBytes())
        });
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        radicadosView.save(radicado, ra, request);

        ArgumentCaptor<Documentos> captor = ArgumentCaptor.forClass(Documentos.class);
        verify(documentosRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo("desconocido");
    }

    @Test
    void saveFallaConUnMensajeClaroCuandoNoSePuedeEscribirEnLaCarpetaDeAdjuntos() throws Exception {
        Path archivoQueOcupaLaCarpeta = Files.createFile(uploadDir.resolve("no-es-carpeta"));
        ReflectionTestUtils.setField(radicadosView, "uploadDir", archivoQueOcupaLaCarpeta.toString());
        Radicados radicado = nuevoRadicado();
        radicado.setId_radicado(2L);
        radicado.setArchivos(new MultipartFile[]{
                new MockMultipartFile("archivos", "oficio.pdf", "application/pdf", "contenido".getBytes())
        });

        assertThatThrownBy(() -> radicadosView.save(radicado, new RedirectAttributesModelMap(), request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("No se pudieron guardar los archivos adjuntos:");
    }

    private static Map<String, Object> flash(RedirectAttributesModelMap ra) {
        return new HashMap<>(ra.getFlashAttributes());
    }

    private Radicados nuevoRadicado() {
        Radicados radicado = new Radicados();
        radicado.setId_radicado(0L);
        return radicado;
    }
}
