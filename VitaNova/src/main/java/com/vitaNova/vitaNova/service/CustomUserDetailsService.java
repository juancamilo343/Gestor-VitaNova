package com.vitaNova.vitaNova.service;

import com.vitaNova.vitaNova.model.Empleados;
import com.vitaNova.vitaNova.model.Usuarios;
import com.vitaNova.vitaNova.repository.EmpleadosRepository;
import com.vitaNova.vitaNova.repository.UsuariosRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    public CustomUserDetailsService(UsuariosRepository usuariosRepository,
                                    EmpleadosRepository empleadosRepository) {
        this.usuariosRepository = usuariosRepository;
        this.empleadosRepository = empleadosRepository;
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

        GrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + nombreRol.toUpperCase());

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(List.of(authority))
                .build();
    }
}