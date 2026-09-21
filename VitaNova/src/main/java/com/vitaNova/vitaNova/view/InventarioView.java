package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Inventario;
import com.vitaNova.vitaNova.model.Lote;
import com.vitaNova.vitaNova.repository.InventarioRepository;
import com.vitaNova.vitaNova.repository.LoteRepository;
import com.vitaNova.vitaNova.repository.ProductosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/view/inventario")
public class InventarioView {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private LoteRepository loteRepository;


    // =========================
    // LISTA
    // =========================
    @GetMapping
    public String lista(
            Model model,
            Authentication authentication) {

        List<Inventario> inventarios =
                inventarioRepository.findAll();

        /*
         * Sincronizamos el stock del inventario
         * con los lotes activos, no vencidos y
         * con cantidad disponible.
         */
        for (Inventario inventario : inventarios) {

            if (inventario.getId_producto() == null) {
                continue;
            }

            int stockLotes =
                    stockDisponibleEnLotes(
                            inventario.getId_producto()
                    );

            /*
             * Si existen lotes disponibles para el producto,
             * los lotes son la fuente real del stock.
             */
            List<Lote> lotes =
                    loteRepository.findByIdProducto(
                            inventario.getId_producto()
                    );

            boolean tieneLotesDisponibles =
                    lotes.stream().anyMatch(lote ->
                            lote.getCantidad() != null
                                    && lote.getCantidad() > 0
                                    && lote.getEstado() == Lote.EstadoLote.ACTIVO
                                    && lote.getFecha_vencimiento() != null
                                    && !lote.getFecha_vencimiento()
                                    .isBefore(LocalDate.now())
                    );

            if (tieneLotesDisponibles) {

                inventario.setStock_actual(stockLotes);

                inventarioRepository.save(inventario);
            }
        }

        /*
         * Volvemos a consultar para que la tabla
         * reciba los valores actualizados.
         */
        inventarios =
                inventarioRepository.findAll();

        model.addAttribute(
                "inventarios",
                inventarios
        );

        model.addAttribute(
                "activeMenu",
                "inventario"
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
                "Gestión de Inventario"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta y seguimiento del inventario de la farmacia."
        );

        // ADMINISTRADOR y FARMACÉUTICO pueden modificar
        model.addAttribute(
                "puedeModificar",
                puedeModificar(authentication)
        );

        return "inventario/inventario";
    }


    // =========================
    // VER LOTES DEL PRODUCTO
    // =========================
    @GetMapping("/lotes/{idProducto}")
    public String lotes(
            @PathVariable Long idProducto,
            Model model,
            RedirectAttributes ra) {

        var producto = productosRepository
                .findById(idProducto)
                .orElse(null);

        if (producto == null) {

            ra.addFlashAttribute(
                    "error",
                    "Producto no encontrado."
            );

            return "redirect:/view/inventario";
        }

        List<Lote> lotes =
                loteRepository.findByIdProducto(idProducto);

        /*
         * El stock total de la pantalla de lotes
         * se obtiene directamente de los lotes.
         */
        Integer stockTotal =
                stockDisponibleEnLotes(idProducto);

        /*
         * También sincronizamos el inventario
         * para mantener ambas partes consistentes.
         */
        Inventario inventario =
                inventarioRepository
                        .buscarPorProducto(idProducto)
                        .orElse(null);

        if (inventario != null) {

            inventario.setStock_actual(stockTotal);

            inventarioRepository.save(inventario);
        }

        model.addAttribute(
                "producto",
                producto
        );

        model.addAttribute(
                "lotes",
                lotes
        );

        model.addAttribute(
                "stockTotal",
                stockTotal
        );

        model.addAttribute(
                "activeMenu",
                "inventario"
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
                "Lotes del producto"
        );

        model.addAttribute(
                "pageSubtitle",
                "Consulta de existencias por lote y fecha de vencimiento."
        );

        return "inventario/inventarioLotes";
    }


    // =========================
    // CALCULAR STOCK DE LOTES
    // =========================
    private int stockDisponibleEnLotes(Long idProducto) {

        List<Lote> lotes =
                loteRepository.findByIdProducto(idProducto);

        LocalDate hoy = LocalDate.now();

        return lotes.stream()
                .filter(lote ->
                        lote.getCantidad() != null
                                && lote.getCantidad() > 0
                )
                .filter(lote ->
                        lote.getEstado() == Lote.EstadoLote.ACTIVO
                )
                .filter(lote ->
                        lote.getFecha_vencimiento() != null
                                && !lote.getFecha_vencimiento()
                                .isBefore(hoy)
                )
                .mapToInt(Lote::getCantidad)
                .sum();
    }


    // =========================
    // FORMULARIO NUEVO
    // =========================
    @GetMapping("/form")
    public String form(
            Model model,
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para registrar inventario."
            );

            return "redirect:/view/inventario";
        }

        Inventario inventario = new Inventario();

        inventario.setStock_actual(0);
        inventario.setStock_minimo(0);
        inventario.setStock_maximo(0);

        model.addAttribute(
                "inventario",
                inventario
        );

        model.addAttribute(
                "productos",
                productosRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Nuevo Inventario"
        );

        model.addAttribute(
                "pageSubtitle",
                "Registre el inventario de un producto existente."
        );

        model.addAttribute(
                "editMode",
                false
        );

        return "inventario/inventarioForm";
    }


    // =========================
    // EDITAR
    // =========================
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para editar inventario."
            );

            return "redirect:/view/inventario";
        }

        Inventario inventario = inventarioRepository
                .findById(id)
                .orElse(null);

        if (inventario == null) {

            ra.addFlashAttribute(
                    "error",
                    "Inventario no encontrado."
            );

            return "redirect:/view/inventario";
        }

        model.addAttribute(
                "inventario",
                inventario
        );

        model.addAttribute(
                "productos",
                productosRepository.findAll()
        );

        model.addAttribute(
                "pageTitle",
                "Editar Inventario"
        );

        model.addAttribute(
                "pageSubtitle",
                "Actualice la información del inventario."
        );

        model.addAttribute(
                "editMode",
                true
        );

        return "inventario/inventarioForm";
    }


    // =========================
    // GUARDAR
    // =========================
    @PostMapping("/save")
    public String save(
            @ModelAttribute Inventario inventario,
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para guardar inventario."
            );

            return "redirect:/view/inventario";
        }

        boolean nuevo =
                inventario.getId_inventario() == null;

        inventarioRepository.save(inventario);

        if (nuevo) {

            ra.addFlashAttribute(
                    "success",
                    "Inventario registrado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "success",
                    "Inventario actualizado con éxito"
            );
        }

        return "redirect:/view/inventario";
    }


    // =========================
    // ELIMINAR
    // =========================
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes ra) {

        if (!puedeModificar(authentication)) {

            ra.addFlashAttribute(
                    "error",
                    "No tiene permisos para eliminar inventario."
            );

            return "redirect:/view/inventario";
        }

        if (inventarioRepository.existsById(id)) {

            inventarioRepository.deleteById(id);

            ra.addFlashAttribute(
                    "success",
                    "Inventario eliminado con éxito"
            );

        } else {

            ra.addFlashAttribute(
                    "error",
                    "Inventario no encontrado."
            );
        }

        return "redirect:/view/inventario";
    }


    // =========================
    // PERMISOS
    // ADMINISTRADOR
    // FARMACEUTICO
    // =========================
    private boolean puedeModificar(
            Authentication authentication) {

        if (authentication == null) {
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