package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Productos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductosRepository extends JpaRepository<Productos, Long>
{
}