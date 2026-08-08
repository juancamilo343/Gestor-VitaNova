package com.vitaNova.vitaNova.security;

import java.util.List;

import com.vitaNova.vitaNova.model.Usuarios;
import com.vitaNova.vitaNova.repository.UsuariosRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService, UserDetailsPasswordService {

    private final UsuariosRepository usuariosRepository;

    public UsuarioDetailsService(UsuariosRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuarios usuario = usuariosRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .disabled(!Boolean.TRUE.equals(usuario.getEstado()))
                .authorities(List.of(new SimpleGrantedAuthority(rolePara(usuario))))
                .build();
    }

    /**
     * Vuelve a almacenar la contrasena con BCrypt cuando el valor guardado seguia
     * en texto plano (migracion transparente en el primer inicio de sesion).
     */
    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        usuariosRepository.findByUsername(user.getUsername()).ifPresent(usuario -> {
            usuario.setPassword(newPassword);
            usuariosRepository.save(usuario);
        });
        return User.withUserDetails(user).password(newPassword).build();
    }

    private String rolePara(Usuarios usuario) {
        String nombre = usuario.getRol() == null ? null : usuario.getRol().getNombre();
        if (nombre == null || nombre.isBlank()) {
            return "ROLE_USER";
        }
        return "ROLE_" + nombre.trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_");
    }
}
