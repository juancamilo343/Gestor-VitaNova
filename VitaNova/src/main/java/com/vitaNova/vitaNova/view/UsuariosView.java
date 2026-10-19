package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Clientes;
import com.vitaNova.vitaNova.model.Empleados;
import com.vitaNova.vitaNova.model.Proveedor;
import com.vitaNova.vitaNova.model.Rol;
import com.vitaNova.vitaNova.model.Usuarios;
import com.vitaNova.vitaNova.repository.ClientesRepository;
import com.vitaNova.vitaNova.repository.EmpleadosRepository;
import com.vitaNova.vitaNova.repository.ProveedorRepository;
import com.vitaNova.vitaNova.repository.RolRepository;
import com.vitaNova.vitaNova.repository.UsuariosRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/view/usuarios")
public class UsuariosView {

    private final UsuariosRepository usuariosRepository;
    private final RolRepository rolRepository;
    private final ClientesRepository clientesRepository;
    private final EmpleadosRepository empleadosRepository;
    private final ProveedorRepository proveedorRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuariosView(
            UsuariosRepository usuariosRepository,
            RolRepository rolRepository,
            ClientesRepository clientesRepository,
            EmpleadosRepository empleadosRepository,
            ProveedorRepository proveedorRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuariosRepository = usuariosRepository;
        this.rolRepository = rolRepository;
        this.clientesRepository = clientesRepository;
        this.empleadosRepository = empleadosRepository;
        this.proveedorRepository = proveedorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // LISTAR USUARIOS
    // =========================================================

    @GetMapping
    public String listarUsuarios(
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String error
    ) {

        if (!puedeModificarUsuarios()) {
            return "redirect:/view/dashboard?error=sinPermisos";
        }

        List<Usuarios> usuarios;

        if (search != null && !search.trim().isEmpty()) {

            usuarios = usuariosRepository.buscarUsuarios(
                    search.trim()
            );

        } else {

            usuarios = usuariosRepository.findAll();
        }

        model.addAttribute(
                "usuarios",
                usuarios
        );

        model.addAttribute(
                "search",
                search
        );

        model.addAttribute(
                "pageTitle",
                "Gestión de Usuarios"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de usuarios del sistema."
        );

        model.addAttribute(
                "puedeModificar",
                puedeModificarUsuarios()
        );

        if ("sinPermisos".equals(error)) {

            model.addAttribute(
                    "error",
                    "No tiene permisos para realizar esta acción."
            );
        }

        return "usuarios/usuarios";
    }

    // =========================================================
    // MOSTRAR FORMULARIO DE REGISTRO
    // =========================================================

    @GetMapping("/form")
    public String mostrarFormulario(Model model) {

        if (!puedeModificarUsuarios()) {

            return "redirect:/view/usuarios?error=sinPermisos";
        }

        model.addAttribute(
                "roles",
                rolesPermitidosParaUsuarioActual()
        );

        model.addAttribute(
                "modoEdicion",
                false
        );

        model.addAttribute(
                "pageTitle",
                "Registrar"
        );

        return "usuarios/UsuariosForm";
    }

    // =========================================================
    // GUARDAR CLIENTE
    // =========================================================

    @PostMapping("/cliente/save")
    public String guardarCliente(
            @Valid @ModelAttribute Clientes cliente,
            BindingResult result,
            Model model
    ) {

        if (!puedeModificarUsuarios()) {

            return "redirect:/view/usuarios?error=sinPermisos";
        }

        if (result.hasErrors()) {

            prepararFormularioConError(
                    model,
                    "cliente",
                    result.getFieldError() != null
                            ? result.getFieldError().getDefaultMessage()
                            : "Verifique los datos ingresados."
            );

            model.addAttribute(
                    "cliente",
                    cliente
            );

            return "usuarios/UsuariosForm";
        }

        if (cliente.getDocumento() != null
                && clientesRepository.existeDocumento(
                cliente.getDocumento().trim()
        )) {

            prepararFormularioConError(
                    model,
                    "cliente",
                    "La cédula ya existe."
            );

            model.addAttribute(
                    "cliente",
                    cliente
            );

            return "usuarios/UsuariosForm";
        }

        cliente.setFecha_registro(
                LocalDate.now()
        );

        clientesRepository.save(cliente);

        return "redirect:/view/usuarios";
    }

    // =========================================================
    // GUARDAR EMPLEADO + USUARIO
    // =========================================================

    @PostMapping("/empleado/save")
    public String guardarEmpleado(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam Long id_rol,
            @RequestParam Boolean estadoUsuario,
            @RequestParam Empleados.EstadoEmpleado estadoEmpleado,
            @Valid @ModelAttribute Empleados empleado,
            BindingResult result,
            Model model
    ) {

        if (!puedeModificarUsuarios()) {

            return "redirect:/view/usuarios?error=sinPermisos";
        }

        if (result.hasErrors()) {

            String mensaje = result.getFieldError() != null
                    ? result.getFieldError().getDefaultMessage()
                    : "Verifique los datos ingresados.";

            prepararFormularioConError(
                    model,
                    "empleado",
                    mensaje
            );

            model.addAttribute(
                    "empleado",
                    empleado
            );

            return "usuarios/UsuariosForm";
        }

        // =====================================================
        // VALIDAR ROL
        // =====================================================

        if (!puedeAsignarRol(id_rol)) {

            prepararFormularioConError(
                    model,
                    "empleado",
                    "No tiene permisos para asignar ese rol."
            );

            model.addAttribute(
                    "empleado",
                    empleado
            );

            return "usuarios/UsuariosForm";
        }

        // =====================================================
        // VALIDAR USERNAME
        // =====================================================

        if (username == null || username.trim().isEmpty()) {

            prepararFormularioConError(
                    model,
                    "empleado",
                    "El nombre de usuario es obligatorio."
            );

            model.addAttribute(
                    "empleado",
                    empleado
            );

            return "usuarios/UsuariosForm";
        }

        if (usuariosRepository.existeUsername(
                username.trim()
        )) {

            prepararFormularioConError(
                    model,
                    "empleado",
                    "El nombre de usuario ya existe."
            );

            model.addAttribute(
                    "empleado",
                    empleado
            );

            return "usuarios/UsuariosForm";
        }

        // =====================================================
        // VALIDAR DOCUMENTO
        // =====================================================

        if (empleado.getDocumento() != null
                && empleadosRepository.existeDocumento(
                empleado.getDocumento().trim()
        )) {

            prepararFormularioConError(
                    model,
                    "empleado",
                    "La cédula ya existe."
            );

            model.addAttribute(
                    "empleado",
                    empleado
            );

            return "usuarios/UsuariosForm";
        }

        // =====================================================
        // CREAR USUARIO
        // =====================================================

        Usuarios usuario = new Usuarios();

        usuario.setUsername(
                username.trim()
        );

        usuario.setPassword(
                passwordEncoder.encode(password)
        );

        usuario.setId_rol(
                id_rol
        );

        usuario.setEstado(
                estadoUsuario
        );

        Usuarios usuarioGuardado =
                usuariosRepository.save(usuario);

        // =====================================================
        // RELACIONAR EMPLEADO CON USUARIO
        // =====================================================

        empleado.setId_usuario(
                usuarioGuardado.getId_usuario()
        );

        empleado.setEstado(
                estadoEmpleado
        );

        empleado.setFecha_ingreso(
                LocalDate.now()
        );

        empleadosRepository.save(
                empleado
        );

        return "redirect:/view/usuarios";
    }

    // =========================================================
    // GUARDAR PROVEEDOR
    // =========================================================

    @PostMapping("/proveedor/save")
    public String guardarProveedor(
            @ModelAttribute Proveedor proveedor,
            Model model
    ) {

        if (!puedeModificarUsuarios()) {

            return "redirect:/view/usuarios?error=sinPermisos";
        }

        if (proveedor.getNombre() == null
                || proveedor.getNombre().trim().isEmpty()) {

            prepararFormularioConError(
                    model,
                    "proveedor",
                    "El nombre del proveedor es obligatorio."
            );

            model.addAttribute(
                    "proveedor",
                    proveedor
            );

            return "usuarios/UsuariosForm";
        }

        proveedorRepository.save(
                proveedor
        );

        return "redirect:/view/usuarios";
    }

    // =========================================================
    // EDITAR USUARIO
    // =========================================================

    @GetMapping("/edit/{id}")
    public String editarUsuario(
            @PathVariable Long id,
            Model model
    ) {

        if (!puedeModificarUsuarios()) {

            return "redirect:/view/usuarios?error=sinPermisos";
        }

        Usuarios usuario =
                usuariosRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Usuario no encontrado"
                                    )
                        );

        if (!puedeAdministrarUsuario(usuario)) {

            return "redirect:/view/usuarios?error=sinPermisos";
        }

        Empleados empleado =
                buscarEmpleadoPorUsuario(
                        usuario.getId_usuario()
                );

        model.addAttribute(
                "usuario",
                usuario
        );

        model.addAttribute(
                "empleado",
                empleado
        );

        model.addAttribute(
                "roles",
                rolesPermitidosParaUsuarioActual()
        );

        model.addAttribute(
                "modoEdicion",
                true
        );

        model.addAttribute(
                "pageTitle",
                "Editar Usuario"
        );

        return "usuarios/UsuariosForm";
    }

    // =========================================================
    // ACTUALIZAR USUARIO
    // =========================================================

    @PostMapping("/update")
    public String actualizarUsuario(
            @RequestParam Long id_usuario,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam Long id_rol,
            @RequestParam Boolean estadoUsuario,
            Model model
    ) {

        if (!puedeModificarUsuarios()) {

            return "redirect:/view/usuarios?error=sinPermisos";
        }

        Usuarios usuario =
                usuariosRepository.findById(id_usuario)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Usuario no encontrado"
                                )
                        );

        if (!puedeAdministrarUsuario(usuario)) {

            return "redirect:/view/usuarios?error=sinPermisos";
        }

        if (!puedeAsignarRol(id_rol)) {

            prepararFormularioEdicionConError(
                    model,
                    usuario,
                    "No tiene permisos para asignar ese rol."
            );

            return "usuarios/UsuariosForm";
        }

        if (username == null
                || username.trim().isEmpty()) {

            prepararFormularioEdicionConError(
                    model,
                    usuario,
                    "El nombre de usuario es obligatorio."
            );

            return "usuarios/UsuariosForm";
        }

        Optional<Usuarios> usuarioExistente =
                usuariosRepository.buscarPorUsername(
                        username.trim()
                );

        if (usuarioExistente.isPresent()
                && !usuarioExistente
                .get()
                .getId_usuario()
                .equals(id_usuario)) {

            prepararFormularioEdicionConError(
                    model,
                    usuario,
                    "El nombre de usuario ya existe."
            );

            return "usuarios/UsuariosForm";
        }

        usuario.setUsername(
                username.trim()
        );

        if (password != null
                && !password.trim().isEmpty()) {

            usuario.setPassword(
                    passwordEncoder.encode(password)
            );
        }

        usuario.setId_rol(
                id_rol
        );

        usuario.setEstado(
                estadoUsuario
        );

        usuariosRepository.save(
                usuario
        );

        return "redirect:/view/usuarios";
    }

    // =========================================================
    // ELIMINAR USUARIO
    // =========================================================

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(
            @PathVariable Long id
    ) {

        if (!puedeModificarUsuarios()) {

            return "redirect:/view/usuarios?error=sinPermisos";
        }

        Usuarios usuario =
                usuariosRepository.findById(id)
                        .orElse(null);

        if (usuario == null) {

            return "redirect:/view/usuarios";
        }

        if (!puedeAdministrarUsuario(usuario)) {

            return "redirect:/view/usuarios?error=sinPermisos";
        }

        usuariosRepository.deleteById(id);

        return "redirect:/view/usuarios";
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private boolean puedeModificarUsuarios() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMINISTRADOR")
                );
    }

    private boolean usuarioActualEsAdministrador() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMINISTRADOR")
                );
    }

    private boolean puedeAsignarRol(Long idRol) {

        if (idRol == null) {

            return false;
        }

        if (usuarioActualEsAdministrador()) {

            return rolRepository.existsById(idRol);
        }

        Rol rol =
                rolRepository.findById(idRol)
                        .orElse(null);

        if (rol == null) {

            return false;
        }

        if (rol.getNombre() == null) {

            return false;
        }

        return !rol.getNombre()
                .equalsIgnoreCase("ADMINISTRADOR");
    }

    private boolean puedeAdministrarUsuario(
            Usuarios usuario
    ) {

        if (usuario == null) {

            return false;
        }

        if (usuarioActualEsAdministrador()) {

            return true;
        }

        Rol rol =
                usuario.getRol();

        if (rol == null) {

            return true;
        }

        if (rol.getNombre() == null) {

            return true;
        }

        return !rol.getNombre()
                .equalsIgnoreCase("ADMINISTRADOR");
    }

    private List<Rol> rolesPermitidosParaUsuarioActual() {

        List<Rol> roles =
                rolRepository.findAll();

        if (usuarioActualEsAdministrador()) {

            return roles;
        }

        return roles.stream()
                .filter(rol ->
                        rol.getNombre() != null
                                && !rol.getNombre()
                                .equalsIgnoreCase("ADMINISTRADOR")
                )
                .toList();
    }

    // =========================================================
    // BUSCAR EMPLEADO
    // =========================================================

    private Empleados buscarEmpleadoPorUsuario(
            Long idUsuario
    ) {

        return empleadosRepository.findAll()
                .stream()
                .filter(e ->
                        e.getId_usuario() != null
                                && e.getId_usuario()
                                .equals(idUsuario)
                )
                .findFirst()
                .orElse(null);
    }

    // =========================================================
    // FORMULARIO CON ERROR
    // =========================================================

    private void prepararFormularioConError(
            Model model,
            String tipoRegistro,
            String mensaje
    ) {

        model.addAttribute(
                "error",
                mensaje
        );

        model.addAttribute(
                "roles",
                rolesPermitidosParaUsuarioActual()
        );

        model.addAttribute(
                "modoEdicion",
                false
        );

        model.addAttribute(
                "pageTitle",
                "Registrar"
        );

        model.addAttribute(
                "tipoRegistro",
                tipoRegistro
        );
    }

    // =========================================================
    // FORMULARIO DE EDICIÓN CON ERROR
    // =========================================================

    private void prepararFormularioEdicionConError(
            Model model,
            Usuarios usuario,
            String mensaje
    ) {

        model.addAttribute(
                "error",
                mensaje
        );

        model.addAttribute(
                "usuario",
                usuario
        );

        model.addAttribute(
                "empleado",
                buscarEmpleadoPorUsuario(
                        usuario.getId_usuario()
                )
        );

        model.addAttribute(
                "roles",
                rolesPermitidosParaUsuarioActual()
        );

        model.addAttribute(
                "modoEdicion",
                true
        );

        model.addAttribute(
                "pageTitle",
                "Editar Usuario"
        );
    }
}