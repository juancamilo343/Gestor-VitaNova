package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Clientes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientesRepository extends JpaRepository<Clientes, Long>
{
}