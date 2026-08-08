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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Controller
public class RadicadosView {

    @Autowired
    private RadicadosRepository radicadosRepository;

    @Autowired
    private DependenciasRepository dependenciasRepository;

    @Autowired
    private SeriesRepository seriesRepository;

    @Autowired
    private SubseriesRepository subseriesRepository;

    @Autowired
    private TramitesRepository tramitesRepository;

    @Autowired
    private EstadosRepository estadosRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private DocumentosRepository documentosRepository;

    // Carpeta donde se guardan fisicamente los archivos adjuntos (configurable en application.properties).
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    // Solo se aceptan adjuntos documentales; nada ejecutable ni interpretable por el navegador.
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "odt", "ods", "txt", "csv", "jpg", "jpeg", "png", "tif", "tiff");

    private static final Set<String> CONTENT_TYPES_PERMITIDOS = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.spreadsheet",
            "text/plain",
            "text/csv",
            "image/jpeg",
            "image/png",
            "image/tiff",
            "application/octet-stream");

    private static final long TAMANO_MAXIMO_BYTES = 20L * 1024 * 1024;

    @GetMapping("/view/radicados")
    public String inicio(Model model) {
        model.addAttribute("radicado", new Radicados());
        cargarCatalogos(model);
        return "radicados/radicacion_Documental";
    }

    @GetMapping("/view/radicados/documental")
    public String documental(Model model) {
        model.addAttribute("radicado", new Radicados());
        cargarCatalogos(model);
        return "radicados/radicacion_Documental";
    }

    /**
     * Carga al modelo todos los catalogos que alimentan los selects de la
     * pantalla de radicacion documental (sin datos quemados).
     */
    private void cargarCatalogos(Model model) {
        model.addAttribute("dependencias", dependenciasRepository.findAll());
        model.addAttribute("series", seriesRepository.findAll());
        model.addAttribute("subseries", subseriesRepository.findAll());
        model.addAttribute("tramites", tramitesRepository.findAll());
        model.addAttribute("estados", estadosRepository.findAll());
        model.addAttribute("usuarios", usuariosRepository.findAll());
    }

    @PostMapping({
            "/view/radicados/save",
            "/view/radicados/documental/save"
    })
    public String save(@ModelAttribute Radicados radicado, RedirectAttributes ra, HttpServletRequest request) {
        boolean isUpdate = radicado.getId_radicado() > 0;
        prepararRadicado(radicado, request.getRequestURI());

        Radicados savedRadicado = radicadosRepository.save(radicado);

        int archivosGuardados = guardarArchivos(savedRadicado, radicado.getArchivos());

        String mensaje = construirMensajeGuardado(isUpdate, request.getRequestURI(), savedRadicado);

        ra.addFlashAttribute("success", mensaje);
        ra.addFlashAttribute("mensaje", mensaje);
        ra.addFlashAttribute("savedRadicado", savedRadicado);
        ra.addFlashAttribute("archivos", archivosGuardados);

        return "redirect:" + obtenerDestinoFormulario(request.getRequestURI());
    }

    @GetMapping("/view/radicados/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Radicados radicado = radicadosRepository.findById(id).orElse(new Radicados());
        model.addAttribute("radicado", radicado);
        cargarCatalogos(model);
        return "radicados/radicacion_Documental";
    }

    @PostMapping("/view/radicados/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        radicadosRepository.deleteById(id);
        ra.addFlashAttribute("success", "Radicacion eliminada con exito");
        ra.addFlashAttribute("mensaje", "Radicacion eliminada con exito");
        return "redirect:/view/radicados/documental";
    }

    private String obtenerDestinoFormulario(String uri) {
        // La radicacion documental es la unica pantalla; siempre se vuelve a ella.
        return "/view/radicados/documental";
    }

    private String construirMensajeGuardado(boolean isUpdate, String uri, Radicados radicado) {
        String accion = isUpdate ? "actualizada" : "guardada";
        return "Radicacion documental " + accion + " con exito";
    }


    private void prepararRadicado(Radicados radicado, String uri) {
        if (!tieneTexto(radicado.getNumero_radicado())) {
            String consecutivo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            radicado.setNumero_radicado("RAD-" + consecutivo);
        }

        if (!tieneTexto(radicado.getFecha_radicado())) {
            String fecha = tieneTexto(radicado.getFechaDocumento())
                    ? radicado.getFechaDocumento()
                    : LocalDateTime.now().toLocalDate().toString();
            radicado.setFecha_radicado(fecha);
        }

        // Clasificacion documental: un select vacio ("") rompe la FK hacia ccd_series/ccd_subseries.
        if (!tieneTexto(radicado.getCodigo_serie())) {
            radicado.setCodigo_serie(null);
        }
        if (!tieneTexto(radicado.getCodigo_subserie())) {
            radicado.setCodigo_subserie(null);
        }

        // === 1. PARSEO SEGURO DE VARIABLES A ENTEROS ===
        Integer tipoPQRSId = convertirAEnteroSeguro(radicado.getTipoPQRS());
        Integer tipoDocId = convertirAEnteroSeguro(radicado.getTipoDocumento());
        Integer prioridadId = convertirAEnteroSeguro(radicado.getPrioridad());

        Integer canalRecepcionId = convertirAEnteroSeguro(radicado.getCanalRecepcion());

        Integer dependenciaId = convertirAEnteroSeguro(radicado.getDependencia());
        Integer depeDestinoId = convertirAEnteroSeguro(radicado.getDependenciaDestino());
        Integer depeOrigenId = convertirAEnteroSeguro(radicado.getDependenciaOrigen());

        Integer responsableId = convertirAEnteroSeguro(radicado.getResponsable());

        // === 2. VALIDACIONES Y ASIGNACIONES ===

        // Trámite
        if (!tieneNumero(radicado.getId_tramite())) {
            radicado.setId_tramite(primerId(tipoPQRSId, tipoDocId, prioridadId, 1));
        }

        // Estado
        if (!tieneNumero(radicado.getId_estado())) {
            radicado.setId_estado(idValorODefecto(canalRecepcionId, 1));
        }

        // Dependencia (CORRECCIÓN CLAVE PARA ENTIDADES RELACIONALES)
        // 1. Evaluamos qué ID numérico corresponde usando tu lógica de prioridades
        Integer idDepElegido = primerId(dependenciaId, depeDestinoId, depeOrigenId, 99);

        // 2. Buscamos el objeto Dependencias real en la base de datos con ese ID
        // NOTA: Cambia "setDependencia" o "setDependencias" según cómo se llame el setter exacto de tu Objeto en la clase Radicados
        Dependencias depBaseDatos = dependenciasRepository.findById(Long.valueOf(idDepElegido)).orElse(null);
        radicado.setDependencias(depBaseDatos);

        // Usuario
        if (!tieneNumero(radicado.getId_usuario())) {
            radicado.setId_usuario(idValorODefecto(responsableId, 2));
        }

        // Criterios de texto restantes
        if (!tieneTexto(radicado.getRemitente())) {
            radicado.setRemitente(valorODefecto(radicado.getResponsable(), "Anónimo"));
        }

        if (!tieneTexto(radicado.getAsunto())) {
            radicado.setAsunto(valorODefecto(radicado.getObservaciones(), "Sin asunto"));
        }
    }

    /**
     * Guarda fisicamente cada archivo adjunto en la carpeta de uploads y registra
     * sus metadatos en la tabla documentos (vinculados al radicado).
     * Devuelve la cantidad de archivos efectivamente guardados.
     */
    private int guardarArchivos(Radicados radicado, MultipartFile[] archivos) {
        if (archivos == null || archivos.length == 0) {
            return 0;
        }

        int guardados = 0;
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(dir);

            String fechaSubida = LocalDateTime.now().toString();

            for (MultipartFile archivo : archivos) {
                if (archivo == null || archivo.isEmpty()) {
                    continue;
                }

                String nombreOriginal = nombreSeguro(archivo.getOriginalFilename());
                String extension = extensionDe(nombreOriginal);

                validarAdjunto(archivo, extension);

                String nombreAlmacenado = radicado.getId_radicado() + "_" + UUID.randomUUID()
                        + (extension.isEmpty() ? "" : "." + extension);

                Path destino = dir.resolve(nombreAlmacenado).normalize();
                if (!destino.startsWith(dir)) {
                    throw new IllegalArgumentException("Ruta de adjunto no valida");
                }
                archivo.transferTo(destino);

                Documentos documento = new Documentos();
                documento.setId_radicado((long) radicado.getId_radicado());
                documento.setTamano((int) archivo.getSize());
                documento.setNombre(nombreOriginal);
                documento.setNombre_archivo(nombreAlmacenado);
                documento.setRuta_archivo(destino.toString());
                documento.setTipo(obtenerTipoArchivo(archivo, nombreOriginal));
                documento.setFecha_subida(fechaSubida);

                documentosRepository.save(documento);
                guardados++;
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudieron guardar los archivos adjuntos: " + e.getMessage(), e);
        }

        return guardados;
    }

    /**
     * Valida tamano, extension y content-type del adjunto antes de escribirlo en disco.
     */
    private void validarAdjunto(MultipartFile archivo, String extension) {
        if (archivo.getSize() > TAMANO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("El adjunto supera el tamano maximo permitido (20MB)");
        }
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new IllegalArgumentException("Tipo de adjunto no permitido: " + extension);
        }
        String contentType = archivo.getContentType();
        if (contentType != null
                && !CONTENT_TYPES_PERMITIDOS.contains(contentType.toLowerCase(Locale.ROOT).split(";")[0].trim())) {
            throw new IllegalArgumentException("Contenido de adjunto no permitido: " + contentType);
        }
    }

    /**
     * Descarta cualquier informacion de ruta enviada por el cliente y deja solo
     * el nombre base saneado del archivo.
     */
    private String nombreSeguro(String nombreOriginal) {
        String limpio = StringUtils.cleanPath(nombreOriginal == null ? "archivo" : nombreOriginal);
        limpio = limpio.replace('\\', '/');
        int barra = limpio.lastIndexOf('/');
        if (barra >= 0) {
            limpio = limpio.substring(barra + 1);
        }
        limpio = limpio.replaceAll("[^A-Za-z0-9._-]", "_");
        while (limpio.startsWith(".")) {
            limpio = limpio.substring(1);
        }
        return limpio.isEmpty() ? "archivo" : limpio;
    }

    private String extensionDe(String nombre) {
        int punto = nombre.lastIndexOf('.');
        return punto >= 0 ? nombre.substring(punto + 1).toLowerCase(Locale.ROOT) : "";
    }

    /**
     * Determina el tipo del archivo: usa el content-type si viene, si no la extension.
     */
    private String obtenerTipoArchivo(MultipartFile archivo, String nombre) {
        if (tieneTexto(archivo.getContentType())) {
            return archivo.getContentType();
        }
        int punto = nombre.lastIndexOf('.');
        return punto >= 0 ? nombre.substring(punto + 1) : "desconocido";
    }

    private String primerTexto(String... valores) {
        for (String valor : valores) {
            if (tieneTexto(valor)) {
                return valor;
            }
        }
        return "";
    }

    private String valorODefecto(String valor, String valorPorDefecto) {
        return tieneTexto(valor) ? valor : valorPorDefecto;
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private boolean tieneNumero(Integer valor) {
        return valor != null && valor > 0;
    }

    /**
     * Convierte un String en Integer de manera segura.
     * Si es nulo, vacío o texto no numérico, retorna null sin lanzar excepciones.
     */
    private Integer convertirAEnteroSeguro(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Evalúa varios IDs en orden de prioridad y devuelve el primero válido (> 0).
     * Si ninguno es válido, devuelve el ID por defecto.
     */
    private Integer primerId(Integer id1, Integer id2, Integer id3, Integer idPorDefecto) {
        if (id1 != null && id1 > 0) return id1;
        if (id2 != null && id2 > 0) return id2;
        if (id3 != null && id3 > 0) return id3;
        return idPorDefecto;
    }

    /**
     * Devuelve el ID proporcionado si es válido, de lo contrario devuelve el ID por defecto.
     */
    private Integer idValorODefecto(Integer valor, Integer idPorDefecto) {
        return (valor != null && valor > 0) ? valor : idPorDefecto;
    }
}