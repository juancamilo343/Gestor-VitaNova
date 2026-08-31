package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Venta;
import com.vitaNova.vitaNova.repository.ClientesRepository;
import com.vitaNova.vitaNova.repository.EmpleadosRepository;
import com.vitaNova.vitaNova.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/view/ventas")
public class VentaView {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ClientesRepository clientesRepository;

    @Autowired
    private EmpleadosRepository empleadosRepository;


    // =====================================================
    // LISTAR VENTAS
    // =====================================================

    @GetMapping
    public String listar(Model model) {

        model.addAttribute(
                "ventas",
                ventaRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Gestión de Ventas"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento de las ventas de la farmacia."
        );

        model.addAttribute(
                "activeMenu",
                "ventas"
        );

        model.addAttribute(
                "userName",
                "Administrador"
        );

        model.addAttribute(
                "userRole",
                "Farmacia Central"
        );

        return "ventas/ventas";
    }


    // =====================================================
    // FORMULARIO NUEVA VENTA
    // =====================================================

    @GetMapping("/form")
    public String form(Model model) {

        Venta venta = new Venta();

        venta.setDescuento(
                java.math.BigDecimal.ZERO
        );

        venta.setImpuestos(
                java.math.BigDecimal.ZERO
        );

        venta.setTotal(
                java.math.BigDecimal.ZERO
        );

        venta.setEstado(
                Venta.Estado.PENDIENTE
        );


        model.addAttribute(
                "venta",
                venta
        );

        model.addAttribute(
                "clientes",
                clientesRepository.findAll()
        );

        model.addAttribute(
                "empleados",
                empleadosRepository.findAll()
        );

        model.addAttribute(
                "estados",
                Venta.Estado.values()
        );


        model.addAttribute(
                "pageTitle",
                "Nueva venta"
        );

        model.addAttribute(
                "pageSubtitle",
                "Registre una nueva venta en la farmacia."
        );

        model.addAttribute(
                "activeMenu",
                "ventas"
        );

        model.addAttribute(
                "userName",
                "Administrador"
        );

        model.addAttribute(
                "userRole",
                "Farmacia Central"
        );


        return "ventas/ventasForm";
    }


    // =====================================================
    // GUARDAR VENTA
    // =====================================================

    @PostMapping("/save")
    public String guardar(
            @ModelAttribute Venta venta,
            RedirectAttributes ra) {

        if (venta.getDescuento() == null) {

            venta.setDescuento(
                    java.math.BigDecimal.ZERO
            );
        }

        if (venta.getImpuestos() == null) {

            venta.setImpuestos(
                    java.math.BigDecimal.ZERO
            );
        }

        if (venta.getTotal() == null) {

            venta.setTotal(
                    java.math.BigDecimal.ZERO
            );
        }

        if (venta.getEstado() == null) {

            venta.setEstado(
                    Venta.Estado.PENDIENTE
            );
        }


        ventaRepository.save(venta);


        ra.addFlashAttribute(
                "success",
                "Venta guardada correctamente."
        );


        return "redirect:/view/ventas";
    }


    // =====================================================
    // EDITAR VENTA
    // =====================================================

    @GetMapping("/edit/{id}")
    public String editar(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra) {

        Venta venta = ventaRepository
                .findById(id)
                .orElse(null);


        if (venta == null) {

            ra.addFlashAttribute(
                    "success",
                    "Venta no encontrada."
            );

            return "redirect:/view/ventas";
        }


        model.addAttribute(
                "venta",
                venta
        );

        model.addAttribute(
                "clientes",
                clientesRepository.findAll()
        );

        model.addAttribute(
                "empleados",
                empleadosRepository.findAll()
        );

        model.addAttribute(
                "estados",
                Venta.Estado.values()
        );


        model.addAttribute(
                "pageTitle",
                "Editar venta"
        );

        model.addAttribute(
                "pageSubtitle",
                "Actualice la información de la venta."
        );

        model.addAttribute(
                "activeMenu",
                "ventas"
        );

        model.addAttribute(
                "userName",
                "Administrador"
        );

        model.addAttribute(
                "userRole",
                "Farmacia Central"
        );


        return "ventas/ventasForm";
    }


    // =====================================================
    // ELIMINAR VENTA
    // =====================================================

    @PostMapping("/delete/{id}")
    public String eliminar(
            @PathVariable Long id,
            RedirectAttributes ra) {

        if (ventaRepository.existsById(id)) {

            ventaRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Venta eliminada correctamente."
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Venta no encontrada."
            );
        }


        return "redirect:/view/ventas";
    }
}