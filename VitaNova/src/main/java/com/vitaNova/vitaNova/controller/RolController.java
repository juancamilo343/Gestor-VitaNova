package com.vitaNova.vitaNova.controller;

import com.vitaNova.vitaNova.model.Rol;
import com.vitaNova.vitaNova.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    @Autowired
    private RolRepository rolRepository;


    // =========================================================
    // LISTAR ROLES
    // =========================================================

    @GetMapping
    public List<Rol> getAll() {

        return rolRepository.findAll();
    }


    // =========================================================
    // OBTENER ROL
    // =========================================================

    @GetMapping("/{id}")
    public Rol getById(@PathVariable Long id) {

        return rolRepository
                .findById(id)
                .orElse(null);
    }


    // =========================================================
    // CREAR ROL
    // =========================================================

    @PostMapping
    public Rol create(@RequestBody Rol rol) {

        return rolRepository.save(rol);
    }


    // =========================================================
    // ACTUALIZAR ROL
    // =========================================================

    @PutMapping("/{id}")
    public Rol update(
            @PathVariable Long id,
            @RequestBody Rol rol) {

        rol.setId_rol(id);

        return rolRepository.save(rol);
    }


    // =========================================================
    // ELIMINAR ROL
    // =========================================================

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {

        rolRepository.deleteById(id);
    }
}