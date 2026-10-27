package com.vitaNova.vitaNova.service;

import com.vitaNova.vitaNova.model.Empleados;
import com.vitaNova.vitaNova.model.Usuarios;
import com.vitaNova.vitaNova.repository.EmpleadosRepository;
import com.vitaNova.vitaNova.repository.UsuariosRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuariosRepository usuariosRepository;
    private final EmpleadosRepository empleadosRepository;
    private final RolPermisosService rolPermisosService;

    public CustomUserDetailsService(UsuariosRepository usuariosRepository,
                                    EmpleadosRepository empleadosRepository,
                                    RolPermisosService rolPermisosService) {
        this.usuariosRepository = usuariosRepository;
        this.empleadosRepository = empleadosRepository;
        this.rolPermisosService = rolPermisosService;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Usuarios usuario = usuariosRepository.buscarPorUsername(username.trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado"));

        if (usuario.getEstado() == null || !usuario.getEstado()) {
            throw new UsernameNotFoundException("Usuario inactivo");
        }

        Empleados empleado = empleadosRepository.findAll()
                .stream()
                .filter(e -> e.getId_usuario() != null
                        && e.getId_usuario().equals(usuario.getId_usuario()))
                .findFirst()
                .orElse(null);

        if (empleado != null
                && empleado.getEstado() == Empleados.EstadoEmpleado.INACTIVO) {

            throw new UsernameNotFoundException("Empleado inactivo");
        }

        String nombreRol = usuario.getRol() != null
                ? usuario.getRol().getNombre()
                : "Usuario";

        Long idRol = usuario.getRol() != null
                ? usuario.getRol().getId_rol()
                : null;

        List<GrantedAuthority> authorities =
                rolPermisosService.construirAutoridades(nombreRol, idRol);

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(authorities)
                .build();
    }
}
