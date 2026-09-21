package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Productos;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import com.vitaNova.vitaNova.repository.CategoriaRepository;
import com.vitaNova.vitaNova.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/view/productos")
public class ProductosView {

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;


    // ==========================================================
    // LISTA DE PRODUCTOS
    // TODOS LOS ROLES PUEDEN VISUALIZAR
    // ==========================================================
    @GetMapping
    public String lista(
            Model model,
            Authentication authentication
    ) {

        model.addAttribute("productos", productosRepository.findAll());

        model.addAttribute("activeMenu", "productos");
        model.addAttribute("userName", "Administrador");
        model.addAttribute("userRole", "Farmacia Central");

        model.addAttribute(
                "pageTitle",
                "Gestión de Productos"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de productos de la farmacia."
        );

        model.addAttribute(
                "puedeModificar",
                puedeModificar(authentication)
        );

        return "productos/productos";
    }


    // ==========================================================
    // FORMULARIO NUEVO PRODUCTO
    // SOLO ADMINISTRADOR Y FARMACÉUTICO
    // ==========================================================
    @GetMapping("/form")
    public String form(
            Model model,
            Authentication authentication,
            RedirectAttributes ra
    ) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para crear productos."
            );

            return "redirect:/view/productos";
        }

        Productos producto = new Productos();

        producto.setEstado("ACTIVO");
        producto.setPrecio_compra(BigDecimal.ZERO);
        producto.setPrecio_venta(BigDecimal.ZERO);

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


    // ==========================================================
    // EDITAR PRODUCTO
    // SOLO ADMINISTRADOR Y FARMACÉUTICO
    // ==========================================================
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            Authentication authentication,
            RedirectAttributes ra
    ) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para editar productos."
            );

            return "redirect:/view/productos";
        }

        Productos producto =
                productosRepository.findById(id).orElse(null);

        if (producto == null) {

            ra.addFlashAttribute(
                    "error",
                    "Producto no encontrado."
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


    // ==========================================================
    // GUARDAR PRODUCTO
    // SOLO ADMINISTRADOR Y FARMACÉUTICO
    // ==========================================================
    @PostMapping("/save")
    public String save(
            @ModelAttribute Productos producto,
            Authentication authentication,
            RedirectAttributes ra
    ) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para modificar productos."
            );

            return "redirect:/view/productos";
        }

        boolean nuevo =
                producto.getId_producto() == null;

        if (producto.getEstado() == null
                || producto.getEstado().isBlank()) {

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


    // ==========================================================
    // ELIMINAR PRODUCTO
    // SOLO ADMINISTRADOR Y FARMACÉUTICO
    // ==========================================================
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes ra
    ) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para eliminar productos."
            );

            return "redirect:/view/productos";
        }

        if (productosRepository.existsById(id)) {

            productosRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Producto eliminado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "error",
                    "Producto no encontrado"
            );
        }

        return "redirect:/view/productos";
    }


    // ==========================================================
    // VALIDAR PERMISOS DE MODIFICACIÓN
    // ADMINISTRADOR + FARMACÉUTICO
    // ==========================================================
    private boolean puedeModificar(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMINISTRADOR")
                                || authority.getAuthority().equals("ROLE_FARMACEUTICO")
                );
    }
}