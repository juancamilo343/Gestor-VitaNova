package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuariosRepository extends JpaRepository<Usuarios, Long>
{
}