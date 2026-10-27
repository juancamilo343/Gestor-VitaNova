package com.vitaNova.vitaNova.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class RolPermisosService {

    private static final String ARCHIVO_PERMISOS = "roles_permisos.properties";
    private final Map<String, Set<String>> permisosPorRol = new HashMap<>();

    public static final Map<String, String> CATALOGO_PERMISOS = new LinkedHashMap<>();

    static {
        CATALOGO_PERMISOS.put("PERM_DASHBOARD", "Dashboard y métricas generales");
        CATALOGO_PERMISOS.put("PERM_VENTAS", "Módulo de Ventas e Historial");
        CATALOGO_PERMISOS.put("PERM_COMPRAS", "Módulo de Compras y Pedidos");
        CATALOGO_PERMISOS.put("PERM_PRODUCTOS", "Módulo de Productos");
        CATALOGO_PERMISOS.put("PERM_INVENTARIO", "Módulo de Inventario y Stock");
        CATALOGO_PERMISOS.put("PERM_CATEGORIAS", "Módulo de Categorías");
        CATALOGO_PERMISOS.put("PERM_LOTES", "Módulo de Lotes y Vencimientos");
        CATALOGO_PERMISOS.put("PERM_PROVEEDORES", "Módulo de Proveedores");
        CATALOGO_PERMISOS.put("PERM_CLIENTES", "Módulo de Clientes");
        CATALOGO_PERMISOS.put("PERM_EMPLEADOS", "Módulo de Empleados");
        CATALOGO_PERMISOS.put("PERM_REPORTES", "Módulo de Reportes");
        CATALOGO_PERMISOS.put("PERM_USUARIOS", "Gestión de Usuarios (Administrativo)");
        CATALOGO_PERMISOS.put("PERM_ROLES", "Parametrización de Roles y Permisos (Administrativo)");
    }

    public RolPermisosService() {
        inicializarPermisosPorDefecto();
        cargarPermisosDesdeArchivo();
    }

    private void inicializarPermisosPorDefecto() {
        // Administrador: Acceso completo
        Set<String> adminPerms = new LinkedHashSet<>(CATALOGO_PERMISOS.keySet());
        permisosPorRol.put("ADMINISTRADOR", adminPerms);
        permisosPorRol.put("ROL_1", new LinkedHashSet<>(adminPerms));

        // Farmacéutico: Operaciones farmacéuticas y compras
        Set<String> farmaceuticoPerms = new LinkedHashSet<>(Arrays.asList(
                "PERM_DASHBOARD",
                "PERM_PRODUCTOS",
                "PERM_INVENTARIO",
                "PERM_CATEGORIAS",
                "PERM_LOTES",
                "PERM_PROVEEDORES",
                "PERM_COMPRAS",
                "PERM_CLIENTES",
                "PERM_REPORTES"
        ));
        permisosPorRol.put("FARMACEUTICO", farmaceuticoPerms);
        permisosPorRol.put("ROL_2", new LinkedHashSet<>(farmaceuticoPerms));

        // Vendedor: Módulo de ventas, clientes y dashboard
        Set<String> vendedorPerms = new LinkedHashSet<>(Arrays.asList(
                "PERM_DASHBOARD",
                "PERM_VENTAS",
                "PERM_CLIENTES"
        ));
        permisosPorRol.put("VENDEDOR", vendedorPerms);
        permisosPorRol.put("ROL_3", new LinkedHashSet<>(vendedorPerms));
    }

    private Path resolverRutaArchivo() {
        Path p = Paths.get(ARCHIVO_PERMISOS);
        if (Files.exists(p)) return p;
        Path sub = Paths.get("VitaNova", ARCHIVO_PERMISOS);
        if (Files.exists(sub)) return sub;
        return p;
    }

    private synchronized void cargarPermisosDesdeArchivo() {
        Path path = resolverRutaArchivo();
        if (!Files.exists(path)) {
            guardarPermisosEnArchivo();
            return;
        }

        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(path);
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            props.load(reader);
            for (String key : props.stringPropertyNames()) {
                String val = props.getProperty(key);
                if (val != null && !val.trim().isEmpty()) {
                    Set<String> set = new LinkedHashSet<>();
                    for (String p : val.split(",")) {
                        String trimP = p.trim();
                        if (!trimP.isEmpty() && CATALOGO_PERMISOS.containsKey(trimP)) {
                            set.add(trimP);
                        }
                    }
                    permisosPorRol.put(normalizarClave(key), set);
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo " + ARCHIVO_PERMISOS + ": " + e.getMessage());
        }
    }

    private synchronized void guardarPermisosEnArchivo() {
        Properties props = new Properties();
        for (Map.Entry<String, Set<String>> entry : permisosPorRol.entrySet()) {
            props.setProperty(entry.getKey(), String.join(",", entry.getValue()));
        }

        Path path = resolverRutaArchivo();
        try (OutputStream os = Files.newOutputStream(path);
             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            props.store(writer, "Permisos parametrizados de Roles - VitaNova PHARMA ERP");
        } catch (IOException e) {
            System.err.println("Error guardando " + ARCHIVO_PERMISOS + ": " + e.getMessage());
        }
    }

    public synchronized Set<String> obtenerPermisos(String rolNombre, Long rolId) {
        Set<String> resultado = new LinkedHashSet<>();

        if (rolId != null) {
            Set<String> porId = permisosPorRol.get("ROL_" + rolId);
            if (porId != null && !porId.isEmpty()) {
                resultado.addAll(porId);
            }
        }

        if (resultado.isEmpty() && rolNombre != null) {
            Set<String> porNombre = permisosPorRol.get(normalizarClave(rolNombre));
            if (porNombre != null && !porNombre.isEmpty()) {
                resultado.addAll(porNombre);
            }
        }

        // Si es ADMINISTRADOR, garantizar siempre todos los permisos administrativos
        if (rolNombre != null && normalizarClave(rolNombre).contains("ADMINISTRADOR")) {
            resultado.add("PERM_ROLES");
            resultado.add("PERM_USUARIOS");
        }

        return resultado;
    }

    public synchronized void guardarPermisosParaRol(Long rolId, String rolNombre, List<String> permisosSeleccionados) {
        Set<String> set = new LinkedHashSet<>();
        if (permisosSeleccionados != null) {
            for (String p : permisosSeleccionados) {
                if (CATALOGO_PERMISOS.containsKey(p)) {
                    set.add(p);
                }
            }
        }

        // Protección obligatoria: ADMINISTRADOR nunca pierde el permiso de parametrizar roles ni usuarios
        if (rolNombre != null && normalizarClave(rolNombre).contains("ADMINISTRADOR")) {
            set.add("PERM_ROLES");
            set.add("PERM_USUARIOS");
        }

        if (rolId != null) {
            permisosPorRol.put("ROL_" + rolId, new LinkedHashSet<>(set));
        }
        if (rolNombre != null) {
            permisosPorRol.put(normalizarClave(rolNombre), new LinkedHashSet<>(set));
        }

        guardarPermisosEnArchivo();
    }

    public List<GrantedAuthority> construirAutoridades(String nombreRol, Long idRol) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        String rolNorm = nombreRol != null ? normalizarClave(nombreRol) : "USUARIO";
        authorities.add(new SimpleGrantedAuthority("ROLE_" + rolNorm));

        Set<String> permisos = obtenerPermisos(nombreRol, idRol);
        for (String perm : permisos) {
            authorities.add(new SimpleGrantedAuthority(perm));
        }

        return authorities;
    }

    public Map<String, String> getCatalogoPermisos() {
        return Collections.unmodifiableMap(CATALOGO_PERMISOS);
    }

    private String normalizarClave(String texto) {
        if (texto == null) return "";
        return texto.trim()
                .toUpperCase()
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replace(" ", "_");
    }
}
