package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Reasignaciones;
import com.vitaNova.vitaNova.repository.ReasignacionesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class ReasignacionesView {


    @Autowired
    private ReasignacionesRepository reasignacionesRepository;

    // Llena la tabla que muestra la info de las reasignaciones
    @GetMapping("/view/reasignaciones")
    public String lista(Model model) {
        model.addAttribute("reasignaciones", reasignacionesRepository.findAll());
        return "reasignaciones/reasignaciones";
    }

    // Muestra el formulario vacío para crear
    @GetMapping("/view/reasignaciones/form")
    public String form(Model model) {
        model.addAttribute("reasignaciones", new Reasignaciones());
        return "reasignaciones/reasignacionesForm";
    }

    // Sirve para guardar la información del formulario
    @PostMapping("/view/reasignaciones/save")
    public String save(@ModelAttribute Reasignaciones reasignaciones, RedirectAttributes ra) {
        reasignacionesRepository.save(reasignaciones);
        ra.addFlashAttribute("success", "Reasignación registrada con éxito");
        return "redirect:/view/reasignaciones";
    }

    // Carga los datos en el formulario para editar
    @GetMapping("/view/reasignaciones/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Reasignaciones reasignaciones = reasignacionesRepository.findById(id).orElse(null);
        if (reasignaciones == null) {
            ra.addFlashAttribute("error", "La reasignacion con id " + id + " no existe");
            return "redirect:/view/reasignaciones";
        }
        model.addAttribute("reasignaciones", reasignaciones);
        return "reasignaciones/reasignacionesForm";
    }

    // Borrar reasignaciones
    @PostMapping("/view/reasignaciones/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        if (!reasignacionesRepository.existsById(id)) {
            ra.addFlashAttribute("error", "La reasignacion con id " + id + " no existe");
            return "redirect:/view/reasignaciones";
        }

        reasignacionesRepository.deleteById(id);
        ra.addFlashAttribute("success", "Reasignación eliminada con éxito");
        return "redirect:/view/reasignaciones";
    }
}