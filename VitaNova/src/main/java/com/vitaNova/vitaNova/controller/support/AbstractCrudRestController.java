package com.vitaNova.vitaNova.controller.support;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * CRUD REST base para entidades con identificador {@code Long}.
 * Las subclases solo declaran su {@code @RequestMapping} y entregan el
 * repositorio junto con el setter del identificador.
 */
public abstract class AbstractCrudRestController<T> {

    private final JpaRepository<T, Long> repository;
    private final BiConsumer<T, Long> idSetter;

    protected AbstractCrudRestController(JpaRepository<T, Long> repository, BiConsumer<T, Long> idSetter) {
        this.repository = repository;
        this.idSetter = idSetter;
    }

    protected JpaRepository<T, Long> repository() {
        return repository;
    }

    @GetMapping
    public List<T> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public T create(@RequestBody T entity) {
        return repository.save(entity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<T> update(@PathVariable Long id, @RequestBody T entity) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        idSetter.accept(entity, id);
        return ResponseEntity.ok(repository.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
