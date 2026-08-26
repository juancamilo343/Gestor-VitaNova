package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Clientes;
import com.vitaNova.vitaNova.repository.ClientesRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/view/clientes")
public class ClientesView {

    @Autowired
    private ClientesRepository clientesRepository;


    // ==========================================
    // LISTA DE CLIENTES
    // ==========================================

    @GetMapping
    public String lista(
            @RequestParam(required = false) String search,
            Model model) {

        if (search == null) {
            search = "";
        }

        if (search.isBlank()) {

            model.addAttribute(
                    "clientes",
                    clientesRepository.findAll()
            );

        } else {

            model.addAttribute(
                    "clientes",
                    clientesRepository
                            .findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrDocumentoContaining(
                                    search,
                                    search,
                                    search
                            )
            );
        }

        model.addAttribute(
                "search",
                search
        );

        // Datos generales del layout
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

        return "clientes/clientes";
    }


    // ==========================================
    // FORMULARIO NUEVO CLIENTE
    // ==========================================

    @GetMapping("/form")
    public String form(Model model) {

        Clientes cliente = new Clientes();

        model.addAttribute(
                "cliente",
                cliente
        );

        model.addAttribute(
                "pageTitle",
                "Nuevo Cliente"
        );

        model.addAttribute(
                "pageSubtitle",
                "Registre un nuevo cliente dentro del sistema VitaNova."
        );

        model.addAttribute(
                "editMode",
                false
        );

        return "clientes/clientesForm";
    }


    // ==========================================
    // EDITAR CLIENTE
    // ==========================================

    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Clientes cliente =
                clientesRepository
                        .findById(id)
                        .orElse(null);

        if (cliente == null) {

            ra.addFlashAttribute(
                    "success",
                    "Cliente no encontrado"
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


    // ==========================================
    // GUARDAR / ACTUALIZAR CLIENTE
    // ==========================================

    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("cliente") Clientes cliente,
            BindingResult result,
            Model model,
            RedirectAttributes ra) {

        // ==========================================
        // VALIDACIONES
        // ==========================================

        if (result.hasErrors()) {

            model.addAttribute(
                    "pageTitle",
                    cliente.getId_cliente() == null
                            ? "Nuevo Cliente"
                            : "Editar Cliente"
            );

            model.addAttribute(
                    "pageSubtitle",
                    cliente.getId_cliente() == null
                            ? "Registre un nuevo cliente dentro del sistema VitaNova."
                            : "Actualice la información del cliente seleccionado."
            );

            model.addAttribute(
                    "editMode",
                    cliente.getId_cliente() != null
            );

            return "clientes/clientesForm";
        }


        // ==========================================
        // DETERMINAR SI ES NUEVO
        // ==========================================

        boolean nuevo =
                cliente.getId_cliente() == null;


        // ==========================================
        // FECHA DE REGISTRO
        // ==========================================

        if (nuevo) {

            cliente.setFecha_registro(
                    LocalDate.now()
            );
        }


        // ==========================================
        // GUARDAR
        // ==========================================

        clientesRepository.save(cliente);


        // ==========================================
        // MENSAJE
        // ==========================================

        if (nuevo) {

            ra.addFlashAttribute(
                    "success",
                    "Cliente registrado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Cliente actualizado con éxito"
            );
        }


        return "redirect:/view/clientes";
    }


    // ==========================================
    // ELIMINAR CLIENTE
    // ==========================================

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes ra) {

        if (clientesRepository.existsById(id)) {

            clientesRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Cliente eliminado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Cliente no encontrado"
            );
        }

        return "redirect:/view/clientes";
    }
}