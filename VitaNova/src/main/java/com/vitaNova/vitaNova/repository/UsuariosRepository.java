package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuariosRepository extends JpaRepository<Usuarios, Long> {

    @Query("""
            SELECT COUNT(u) > 0
            FROM Usuarios u
            WHERE u.username = :username
            """)
    boolean existeUsername(@Param("username") String username);

    @Query("""
            SELECT u
            FROM Usuarios u
            WHERE u.username = :username
            """)
    Optional<Usuarios> buscarPorUsername(
            @Param("username") String username
    );
}