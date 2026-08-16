package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long>
{
}