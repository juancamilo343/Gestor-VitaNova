package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Clientes;
import com.vitaNova.vitaNova.model.Empleados;
import com.vitaNova.vitaNova.model.Proveedor;
import com.vitaNova.vitaNova.model.Usuarios;
import com.vitaNova.vitaNova.repository.ClientesRepository;
import com.vitaNova.vitaNova.repository.EmpleadosRepository;
import com.vitaNova.vitaNova.repository.ProveedorRepository;
import com.vitaNova.vitaNova.repository.RolRepository;
import com.vitaNova.vitaNova.repository.UsuariosRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/view/usuarios")
public class UsuariosView {

    private final UsuariosRepository usuariosRepository;
    private final RolRepository rolRepository;
    private final ClientesRepository clientesRepository;
    private final EmpleadosRepository empleadosRepository;
    private final ProveedorRepository proveedorRepository;

    public UsuariosView(
            UsuariosRepository usuariosRepository,
            RolRepository rolRepository,
            ClientesRepository clientesRepository,
            EmpleadosRepository empleadosRepository,
            ProveedorRepository proveedorRepository
    ) {
        this.usuariosRepository = usuariosRepository;
        this.rolRepository = rolRepository;
        this.clientesRepository = clientesRepository;
        this.empleadosRepository = empleadosRepository;
        this.proveedorRepository = proveedorRepository;
    }

    @GetMapping
    public String listarUsuarios(Model model) {

        model.addAttribute(
                "usuarios",
                usuariosRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Gestión de Usuarios"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de usuarios del sistema."
        );

        return "usuarios/usuarios";
    }

    @GetMapping("/form")
    public String mostrarFormulario(Model model) {

        model.addAttribute(
                "roles",
                rolRepository.findAll()
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

    @PostMapping("/cliente/save")
    public String guardarCliente(
            @Valid @ModelAttribute Clientes cliente,
            BindingResult result,
            Model model
    ) {

        if (result.hasErrors()) {

            model.addAttribute(
                    "roles",
                    rolRepository.findAll()
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
                    "cliente"
            );

            String mensaje = result.getFieldError() != null
                    ? result.getFieldError().getDefaultMessage()
                    : "Verifique los datos ingresados.";

            model.addAttribute(
                    "error",
                    mensaje
            );

            return "usuarios/UsuariosForm";
        }

        if (cliente.getDocumento() != null
                && clientesRepository.existeDocumento(
                cliente.getDocumento().trim()
        )) {

            model.addAttribute(
                    "roles",
                    rolRepository.findAll()
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
                    "cliente"
            );

            model.addAttribute(
                    "error",
                    "La cédula ya existe."
            );

            return "usuarios/UsuariosForm";
        }

        cliente.setFecha_registro(
                LocalDate.now()
        );

        clientesRepository.save(cliente);

        return "redirect:/view/usuarios";
    }

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

        if (result.hasErrors()) {

            String mensaje = result.getFieldError() != null
                    ? result.getFieldError().getDefaultMessage()
                    : "Verifique los datos ingresados.";

            model.addAttribute(
                    "error",
                    mensaje
            );

            model.addAttribute(
                    "roles",
                    rolRepository.findAll()
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
                    "empleado"
            );

            return "usuarios/UsuariosForm";
        }

        /*
         * Validar username duplicado
         */
        if (usuariosRepository.existeUsername(
                username.trim()
        )) {

            model.addAttribute(
                    "error",
                    "El nombre de usuario ya existe."
            );

            model.addAttribute(
                    "roles",
                    rolRepository.findAll()
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
                    "empleado"
            );

            return "usuarios/UsuariosForm";
        }

        /*
         * Validar documento duplicado
         */
        if (empleado.getDocumento() != null
                && empleadosRepository.existeDocumento(
                empleado.getDocumento().trim()
        )) {

            model.addAttribute(
                    "error",
                    "La cédula ya existe."
            );

            model.addAttribute(
                    "roles",
                    rolRepository.findAll()
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
                    "empleado"
            );

            return "usuarios/UsuariosForm";
        }

        /*
         * Crear usuario
         */
        Usuarios usuario = new Usuarios();

        usuario.setUsername(
                username.trim()
        );

        usuario.setPassword(
                password
        );

        usuario.setId_rol(
                id_rol
        );

        usuario.setEstado(
                estadoUsuario
        );

        Usuarios usuarioGuardado =
                usuariosRepository.save(usuario);

        /*
         * Relacionar empleado con usuario
         */
        empleado.setId_usuario(
                usuarioGuardado.getId_usuario()
        );

        empleado.setEstado(
                estadoEmpleado
        );

        empleadosRepository.save(
                empleado
        );

        return "redirect:/view/usuarios";
    }

    @PostMapping("/proveedor/save")
    public String guardarProveedor(
            @ModelAttribute Proveedor proveedor,
            Model model
    ) {

        if (proveedor.getNombre() == null
                || proveedor.getNombre().trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "El nombre del proveedor es obligatorio."
            );

            model.addAttribute(
                    "roles",
                    rolRepository.findAll()
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
                    "proveedor"
            );

            return "usuarios/UsuariosForm";
        }

        proveedorRepository.save(
                proveedor
        );

        return "redirect:/view/usuarios";
    }

    @GetMapping("/edit/{id}")
    public String editarUsuario(
            @PathVariable Long id,
            Model model
    ) {

        Usuarios usuario =
                usuariosRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Usuario no encontrado"
                                )
                        );

        Empleados empleado =
                empleadosRepository.findAll()
                        .stream()
                        .filter(e ->
                                e.getId_usuario() != null
                                        && e.getId_usuario()
                                        .equals(
                                                usuario.getId_usuario()
                                        )
                        )
                        .findFirst()
                        .orElse(null);

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
                rolRepository.findAll()
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

    @PostMapping("/update")
    public String actualizarUsuario(
            @RequestParam Long id_usuario,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam Long id_rol,
            @RequestParam Boolean estadoUsuario,
            Model model
    ) {

        Usuarios usuario =
                usuariosRepository.findById(id_usuario)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Usuario no encontrado"
                                )
                        );

        Optional<Usuarios> usuarioExistente =
                usuariosRepository.buscarPorUsername(
                        username.trim()
                );

        if (usuarioExistente.isPresent()
                && !usuarioExistente
                .get()
                .getId_usuario()
                .equals(id_usuario)) {

            Empleados empleado =
                    empleadosRepository.findAll()
                            .stream()
                            .filter(e ->
                                    e.getId_usuario() != null
                                            && e.getId_usuario()
                                            .equals(id_usuario)
                            )
                            .findFirst()
                            .orElse(null);

            model.addAttribute(
                    "error",
                    "El nombre de usuario ya existe."
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
                    rolRepository.findAll()
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

        usuario.setUsername(
                username.trim()
        );

        if (password != null
                && !password.trim().isEmpty()) {

            usuario.setPassword(
                    password
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

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(
            @PathVariable Long id
    ) {

        usuariosRepository.deleteById(id);

        return "redirect:/view/usuarios";
    }
}