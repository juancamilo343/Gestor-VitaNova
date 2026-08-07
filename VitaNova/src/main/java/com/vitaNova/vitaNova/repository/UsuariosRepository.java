package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsuariosRepository extends JpaRepository<Usuarios, Long> {

    @Query("SELECT u FROM Usuarios u " +
           "WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(u.num_identificacion) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Usuarios> search(@Param("query") String query);
}
