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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    // =========================================================
    // LISTAR USUARIOS
    // =========================================================

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

    // =========================================================
    // FORMULARIO NUEVO
    // =========================================================

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

    // =========================================================
    // GUARDAR CLIENTE
    // =========================================================

    @PostMapping("/cliente/save")
    public String guardarCliente(
            @ModelAttribute Clientes cliente
    ) {

        cliente.setFecha_registro(LocalDate.now());

        clientesRepository.save(cliente);

        return "redirect:/view/usuarios";
    }

    // =========================================================
    // GUARDAR EMPLEADO
    // =========================================================

    @PostMapping("/empleado/save")
    public String guardarEmpleado(

            @RequestParam String username,

            @RequestParam String password,

            @RequestParam Long id_rol,

            @RequestParam Boolean estadoUsuario,

            @RequestParam Empleados.EstadoEmpleado estadoEmpleado,

            @ModelAttribute Empleados empleado
    ) {

        Usuarios usuario = new Usuarios();

        usuario.setUsername(username);
        usuario.setPassword(password);
        usuario.setId_rol(id_rol);
        usuario.setEstado(estadoUsuario);

        Usuarios usuarioGuardado =
                usuariosRepository.save(usuario);

        empleado.setId_usuario(
                usuarioGuardado.getId_usuario()
        );

        empleado.setEstado(estadoEmpleado);

        empleadosRepository.save(empleado);

        return "redirect:/view/usuarios";
    }

    // =========================================================
    // GUARDAR PROVEEDOR
    // =========================================================

    @PostMapping("/proveedor/save")
    public String guardarProveedor(
            @ModelAttribute Proveedor proveedor
    ) {

        proveedorRepository.save(proveedor);

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

        Usuarios usuario = usuariosRepository
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Usuario no encontrado"
                        )
                );

        // Buscar empleado relacionado con este usuario
        Empleados empleado = empleadosRepository
                .findAll()
                .stream()
                .filter(e ->
                        e.getId_usuario() != null &&
                                e.getId_usuario().equals(
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

    // =========================================================
    // ACTUALIZAR USUARIO
    // =========================================================

    @PostMapping("/update")
    public String actualizarUsuario(

            @RequestParam Long id_usuario,

            @RequestParam String username,

            @RequestParam String password,

            @RequestParam Long id_rol,

            @RequestParam Boolean estadoUsuario
    ) {

        Usuarios usuario = usuariosRepository
                .findById(id_usuario)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Usuario no encontrado"
                        )
                );

        usuario.setUsername(username);

        /*
         * Solo actualizamos la contraseña si
         * el usuario escribió una nueva.
         */
        if (password != null && !password.trim().isEmpty()) {
            usuario.setPassword(password);
        }

        usuario.setId_rol(id_rol);
        usuario.setEstado(estadoUsuario);

        usuariosRepository.save(usuario);

        return "redirect:/view/usuarios";
    }

    // =========================================================
    // ELIMINAR USUARIO
    // =========================================================

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(
            @PathVariable Long id
    ) {

        usuariosRepository.deleteById(id);

        return "redirect:/view/usuarios";
    }
}