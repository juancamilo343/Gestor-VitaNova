package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Productos;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import com.vitaNova.vitaNova.repository.CategoriaRepository;
import com.vitaNova.vitaNova.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/view/productos")
public class ProductosView {

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    // =========================
    // LISTA
    // Vista: productos/productos.html
    // =========================
    @GetMapping
    public String lista(Model model) {

        model.addAttribute(
                "productos",
                productosRepository.findAll()
        );

        model.addAttribute(
                "activeMenu",
                "productos"
        );

        model.addAttribute(
                "userName",
                "Administrador"
        );

        model.addAttribute(
                "userRole",
                "Farmacia Central"
        );

        model.addAttribute(
                "pageTitle",
                "Gestión de Productos"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de productos de la farmacia."
        );

        return "productos/productos";
    }

    // =========================
    // FORMULARIO NUEVO PRODUCTO
    // =========================
    @GetMapping("/form")
    public String form(Model model) {

        Productos producto = new Productos();

        producto.setEstado("ACTIVO");
        producto.setPrecio_compra(0.0);
        producto.setPrecio_venta(0.0);

        model.addAttribute(
                "producto",
                producto
        );

        model.addAttribute(
                "categorias",
                categoriaRepository.findAll()
        );

        model.addAttribute(
                "proveedores",
                proveedorRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Nuevo Producto"
        );

        model.addAttribute(
                "pageSubtitle",
                "Registre un nuevo producto dentro del sistema VitaNova."
        );

        model.addAttribute(
                "editMode",
                false
        );

        return "productos/productosForm";
    }

    // =========================
    // EDITAR PRODUCTO
    // =========================
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Productos producto = productosRepository
                .findById(id)
                .orElse(null);

        if (producto == null) {

            ra.addFlashAttribute(
                    "success",
                    "Producto no encontrado"
            );

            return "redirect:/view/productos";
        }

        model.addAttribute(
                "producto",
                producto
        );

        model.addAttribute(
                "categorias",
                categoriaRepository.findAll()
        );

        model.addAttribute(
                "proveedores",
                proveedorRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Editar Producto"
        );

        model.addAttribute(
                "pageSubtitle",
                "Actualice la información del producto seleccionado."
        );

        model.addAttribute(
                "editMode",
                true
        );

        return "productos/productosForm";
    }

    // =========================
    // GUARDAR / ACTUALIZAR
    // =========================
    @PostMapping("/save")
    public String save(
            @ModelAttribute Productos producto,
            RedirectAttributes ra) {

        boolean nuevo = producto.getId_producto() == null;

        if (producto.getEstado() == null ||
                producto.getEstado().isBlank()) {

            producto.setEstado("ACTIVO");
        }

        productosRepository.save(producto);

        if (nuevo) {

            ra.addFlashAttribute(
                    "success",
                    "Producto registrado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Producto actualizado con éxito"
            );
        }

        return "redirect:/view/productos";
    }

    // =========================
    // ELIMINAR PRODUCTO
    // =========================
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes ra) {

        if (productosRepository.existsById(id)) {

            productosRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Producto eliminado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Producto no encontrado"
            );
        }

        return "redirect:/view/productos";
    }
}