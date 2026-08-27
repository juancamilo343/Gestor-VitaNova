package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Empleados;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpleadosRepository extends JpaRepository<Empleados, Long>
{
    boolean existsByDocumento(String documento);
}