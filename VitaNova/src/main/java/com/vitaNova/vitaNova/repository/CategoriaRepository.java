package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // ==========================================================
    // BUSCAR CATEGORÍAS POR NOMBRE
    // ==========================================================
    @Query("""
            SELECT c
            FROM Categoria c
            WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    List<Categoria> buscarCategorias(
            @Param("search") String search
    );
}