package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Clientes;
import com.vitaNova.vitaNova.repository.ClientesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/view/clientes")
public class ClientesView {

    @Autowired
    private ClientesRepository clientesRepository;


    // ==========================================================
    // LISTA DE CLIENTES
    // TODOS LOS ROLES PUEDEN VISUALIZAR
    // ==========================================================
    @GetMapping
    public String lista(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) String search
    ) {

        if (search != null && !search.trim().isEmpty()) {

            model.addAttribute(
                    "clientes",
                    clientesRepository.buscarClientes(
                            search.trim()
                    )
            );

        } else {

            model.addAttribute(
                    "clientes",
                    clientesRepository.findAll()
            );
        }

        model.addAttribute(
                "search",
                search
        );

        model.addAttribute(
                "activeMenu",
                "clientes"
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
                "Gestión de Clientes"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de los clientes de la farmacia."
        );

        model.addAttribute(
                "puedeModificar",
                puedeModificar(authentication)
        );

        model.addAttribute(
                "puedeCrear",
                puedeCrear(authentication)
        );

        return "clientes/clientes";
    }


    // ==========================================================
    // EDITAR CLIENTE
    // ADMINISTRADOR + FARMACÉUTICO + VENDEDOR
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
                    "No tiene permisos para editar clientes."
            );

            return "redirect:/view/clientes";
        }

        Clientes cliente =
                clientesRepository.findById(id).orElse(null);

        if (cliente == null) {

            ra.addFlashAttribute(
                    "error",
                    "Cliente no encontrado."
            );

            return "redirect:/view/clientes";
        }

        model.addAttribute(
                "cliente",
                cliente
        );

        model.addAttribute(
                "pageTitle",
                "Editar Cliente"
        );

        model.addAttribute(
                "pageSubtitle",
                "Actualice la información del cliente seleccionado."
        );

        model.addAttribute(
                "editMode",
                true
        );

        return "clientes/clientesForm";
    }


    // ==========================================================
    // GUARDAR / ACTUALIZAR CLIENTE
    // LOS CLIENTES NUEVOS SIGUEN REGISTRÁNDOSE DESDE USUARIOS
    // ==========================================================
    @PostMapping("/save")
    public String save(
            @ModelAttribute Clientes cliente,
            Authentication authentication,
            RedirectAttributes ra
    ) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para modificar clientes."
            );

            return "redirect:/view/clientes";
        }

        // Los clientes nuevos deben registrarse
        // desde el módulo de Usuarios.
        if (cliente.getId_cliente() == null) {

            ra.addFlashAttribute(
                    "error",
                    "Los clientes nuevos deben registrarse desde el módulo de Usuarios."
            );

            return "redirect:/view/clientes";
        }

        if (!clientesRepository.existsById(
                cliente.getId_cliente()
        )) {

            ra.addFlashAttribute(
                    "error",
                    "Cliente no encontrado."
            );

            return "redirect:/view/clientes";
        }

        clientesRepository.save(cliente);

        ra.addFlashAttribute(
                "success",
                "Cliente actualizado con éxito"
        );

        return "redirect:/view/clientes";
    }


    // ==========================================================
    // ELIMINAR CLIENTE
    // ADMINISTRADOR + FARMACÉUTICO + VENDEDOR
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
                    "No tiene permisos para eliminar clientes."
            );

            return "redirect:/view/clientes";
        }

        if (clientesRepository.existsById(id)) {

            clientesRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Cliente eliminado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "error",
                    "Cliente no encontrado"
            );
        }

        return "redirect:/view/clientes";
    }


    // ==========================================================
    // PERMISO DE MODIFICACIÓN
    // ADMINISTRADOR + FARMACÉUTICO + VENDEDOR
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

                        authority.getAuthority().equals(
                                "ROLE_ADMINISTRADOR"
                        )

                                || authority.getAuthority().equals(
                                "ROLE_FARMACEUTICO"
                        )

                                || authority.getAuthority().equals(
                                "ROLE_VENDEDOR"
                        )
                );
    }


    // ==========================================================
    // PERMISO PARA MOSTRAR "NUEVO CLIENTE"
    //
    // El registro real continúa haciéndose mediante Usuarios.
    // Solo ADMINISTRADOR y FARMACÉUTICO pueden administrar
    // ese registro centralizado.
    // ==========================================================
    private boolean puedeCrear(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->

                        authority.getAuthority().equals(
                                "ROLE_ADMINISTRADOR"
                        )

                                || authority.getAuthority().equals(
                                "ROLE_FARMACEUTICO"
                        )
                );
    }
}