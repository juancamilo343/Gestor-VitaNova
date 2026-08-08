package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuariosRepository extends JpaRepository<Usuarios, Long> {

    Optional<Usuarios> findByUsername(String username);

    @Query("SELECT u FROM Usuarios u LEFT JOIN u.rol r " +
           "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(r.nombre) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Usuarios> search(@Param("query") String query);
}
