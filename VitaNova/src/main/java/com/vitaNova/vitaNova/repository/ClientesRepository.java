package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Clientes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientesRepository extends JpaRepository<Clientes, Long> {

    List<Clientes> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrDocumentoContaining(
            String nombres,
            String apellidos,
            String documento
    );
}