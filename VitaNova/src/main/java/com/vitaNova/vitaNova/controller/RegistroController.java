package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Empleados;
import com.vitaNova.vitaNova.model.Rol;
import com.vitaNova.vitaNova.model.Usuarios;
import com.vitaNova.vitaNova.repository.EmpleadosRepository;
import com.vitaNova.vitaNova.repository.RolRepository;
import com.vitaNova.vitaNova.repository.UsuariosRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/registro")
public class RegistroController {

    private final UsuariosRepository usuariosRepository;
    private final EmpleadosRepository empleadosRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroController(
            UsuariosRepository usuariosRepository,
            EmpleadosRepository empleadosRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuariosRepository = usuariosRepository;
        this.empleadosRepository = empleadosRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String mostrarRegistro(Model model) {

        model.addAttribute(
                "empleado",
                new Empleados()
        );

        return "registro";
    }

    @PostMapping("/save")
    public String registrar(
            @Valid @ModelAttribute("empleado") Empleados empleado,
            BindingResult result,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String passwordConfirm,
            Model model
    ) {

        username = username != null
                ? username.trim()
                : "";

        /*
         * Validaciones del empleado.
         */
        if (result.hasErrors()) {

            String mensaje = result.getFieldError() != null
                    ? result.getFieldError().getDefaultMessage()
                    : "Verifique los datos ingresados.";

            model.addAttribute(
                    "error",
                    mensaje
            );

            return "registro";
        }

        /*
         * Validar username vacío.
         */
        if (username.isEmpty()) {

            model.addAttribute(
                    "error",
                    "El nombre de usuario es obligatorio."
            );

            return "registro";
        }

        /*
         * Validar contraseña.
         */
        if (password == null
                || password.trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "La contraseña es obligatoria."
            );

            return "registro";
        }

        /*
         * Validar confirmación de contraseña.
         */
        if (passwordConfirm == null
                || !password.equals(passwordConfirm)) {

            model.addAttribute(
                    "error",
                    "Las contraseñas no coinciden."
            );

            return "registro";
        }

        /*
         * Validar longitud mínima de contraseña.
         */
        if (password.length() < 6) {

            model.addAttribute(
                    "error",
                    "La contraseña debe tener mínimo 6 caracteres."
            );

            return "registro";
        }

        /*
         * Validar username duplicado.
         */
        if (usuariosRepository.existeUsername(username)) {

            model.addAttribute(
                    "error",
                    "El nombre de usuario ya existe."
            );

            return "registro";
        }

        /*
         * Validar documento duplicado.
         */
        if (empleadosRepository.existeDocumento(
                empleado.getDocumento().trim()
        )) {

            model.addAttribute(
                    "error",
                    "La cédula ya está registrada."
            );

            return "registro";
        }

        /*
         * Buscar el rol VENDEDOR.
         *
         * El usuario NO puede escoger el rol.
         */
        Rol rolVendedor = rolRepository.findAll()
                .stream()
                .filter(rol ->
                        rol.getNombre() != null
                                && rol.getNombre()
                                .equalsIgnoreCase(
                                        "VENDEDOR"
                                )
                )
                .findFirst()
                .orElse(null);

        if (rolVendedor == null) {

            model.addAttribute(
                    "error",
                    "No se encontró el rol VENDEDOR en el sistema."
            );

            return "registro";
        }

        /*
         * Crear usuario.
         */
        Usuarios usuario = new Usuarios();

        usuario.setUsername(username);

        /*
         * Encriptar contraseña con BCrypt.
         */
        usuario.setPassword(
                passwordEncoder.encode(password)
        );

        /*
         * Rol automático:
         * VENDEDOR
         */
        usuario.setId_rol(
                rolVendedor.getId_rol()
        );

        /*
         * Cuenta activa.
         */
        usuario.setEstado(true);

        Usuarios usuarioGuardado =
                usuariosRepository.save(usuario);

        /*
         * Asociar empleado con usuario.
         */
        empleado.setId_usuario(
                usuarioGuardado.getId_usuario()
        );

        /*
         * Empleado activo.
         */
        empleado.setEstado(
                Empleados.EstadoEmpleado.ACTIVO
        );

        /*
         * Fecha de ingreso automática.
         */
        empleado.setFecha_ingreso(
                LocalDate.now()
        );

        empleadosRepository.save(empleado);

        /*
         * Registro exitoso.
         */
        return "redirect:/login?registro=exitoso";
    }
}