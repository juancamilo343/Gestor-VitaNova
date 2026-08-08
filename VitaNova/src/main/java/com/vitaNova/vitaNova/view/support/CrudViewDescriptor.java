package com.vitaNova.vitaNova.view.support;

/**
 * Datos propios de cada pantalla CRUD: rutas, nombres de plantillas, atributos
 * del modelo y etiqueta usada en los mensajes de confirmacion.
 *
 * @param basePath      ruta base de la pantalla, por ejemplo {@code /view/series}
 * @param listView      plantilla del listado
 * @param formView      plantilla del formulario
 * @param listAttribute atributo del modelo con la coleccion
 * @param formAttribute atributo del modelo con la entidad del formulario
 * @param label         etiqueta singular usada en los mensajes ("Serie")
 * @param feminine      true cuando la etiqueta es femenina ("registrada" vs "registrado")
 */
public record CrudViewDescriptor(
        String basePath,
        String listView,
        String formView,
        String listAttribute,
        String formAttribute,
        String label,
        boolean feminine) {

    public CrudViewDescriptor(String basePath, String listView, String formView, String attribute,
                              String label, boolean feminine) {
        this(basePath, listView, formView, attribute, attribute, label, feminine);
    }

    public String redirectToList() {
        return "redirect:" + basePath;
    }

    public String createdMessage() {
        return message("registrad");
    }

    public String updatedMessage() {
        return message("actualizad");
    }

    public String deletedMessage() {
        return message("eliminad");
    }

    public String notFoundMessage() {
        return label + (feminine ? " no encontrada" : " no encontrado");
    }

    private String message(String verbStem) {
        return label + " " + verbStem + (feminine ? "a" : "o") + " con exito";
    }
}
