package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
}