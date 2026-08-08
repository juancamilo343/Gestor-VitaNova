package com.vitaNova.vitaNova.view.support;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * CRUD Thymeleaf base (listar, formulario, guardar, editar, eliminar) para las
 * pantallas de mantenimiento. Las subclases declaran su
 * {@code @RequestMapping("/view/<entidad>")} y entregan el repositorio, el
 * constructor de la entidad y el descriptor de la pantalla.
 */
public abstract class AbstractCrudViewController<T> {

    private final JpaRepository<T, Long> repository;
    private final Supplier<T> entityFactory;
    private final Function<T, Long> idGetter;
    private final CrudViewDescriptor descriptor;

    protected AbstractCrudViewController(JpaRepository<T, Long> repository,
                                         Supplier<T> entityFactory,
                                         Function<T, Long> idGetter,
                                         CrudViewDescriptor descriptor) {
        this.repository = repository;
        this.entityFactory = entityFactory;
        this.idGetter = idGetter;
        this.descriptor = descriptor;
    }

    protected JpaRepository<T, Long> repository() {
        return repository;
    }

    protected CrudViewDescriptor descriptor() {
        return descriptor;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute(descriptor.listAttribute(), repository.findAll());
        populateListModel(model);
        return descriptor.listView();
    }

    @GetMapping("/form")
    public String form(Model model) {
        model.addAttribute(descriptor.formAttribute(), entityFactory.get());
        populateFormModel(model);
        return descriptor.formView();
    }

    @PostMapping("/save")
    public String save(@ModelAttribute T entity, RedirectAttributes ra) {
        Long id = idGetter.apply(entity);
        boolean nuevo = id == null || id == 0L;

        T saved = repository.save(entity);

        addMessage(ra, nuevo ? descriptor.createdMessage() : descriptor.updatedMessage());
        afterSave(saved, ra);
        return descriptor.redirectToList();
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return repository.findById(id)
                .map(entity -> {
                    model.addAttribute(descriptor.formAttribute(), entity);
                    populateFormModel(model);
                    return descriptor.formView();
                })
                .orElseGet(() -> {
                    addMessage(ra, descriptor.notFoundMessage());
                    return descriptor.redirectToList();
                });
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            addMessage(ra, descriptor.deletedMessage());
        } else {
            addMessage(ra, descriptor.notFoundMessage());
        }
        return descriptor.redirectToList();
    }

    /** Atributos extra del listado (catalogos, KPIs, etc.). */
    protected void populateListModel(Model model) {
    }

    /** Atributos extra del formulario (listas de seleccion, etc.). */
    protected void populateFormModel(Model model) {
    }

    /** Gancho para exponer datos del registro guardado en el listado. */
    protected void afterSave(T saved, RedirectAttributes ra) {
    }

    /**
     * Las plantillas leen indistintamente {@code success} o {@code mensaje},
     * por lo que ambos atributos se publican con el mismo texto.
     */
    private void addMessage(RedirectAttributes ra, String mensaje) {
        ra.addFlashAttribute("success", mensaje);
        ra.addFlashAttribute("mensaje", mensaje);
    }
}
