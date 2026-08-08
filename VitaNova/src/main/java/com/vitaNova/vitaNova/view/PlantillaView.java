package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.exception.RecursoNoEncontradoException;
import com.vitaNova.vitaNova.model.Plantilla;
import com.vitaNova.vitaNova.repository.PlantillaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PlantillaView
{
    @Autowired
    private PlantillaRepository plantillaRepository;

    // LISTA
    @GetMapping("/view/plantilla")
    public String lista(Model model)
    {
        model.addAttribute("plantilla", plantillaRepository.findAll());

        return "Plantilla/Plantilla";
    }

    // FORMULARIO
    @GetMapping("/view/bitacoras/form")
    public String form(Model model)
    {
        model.addAttribute("plantilla", new Plantilla());

        return "Plantilla/PlantillaForm";
    }

    // GUARDAR
    @PostMapping("/view/bitacoras/save")
    public String save(@ModelAttribute Plantilla bitacora,
                       RedirectAttributes ra)
    {
        plantillaRepository.save(bitacora);

        ra.addFlashAttribute("success",
                "Plantilla registrada con éxito");

        return "redirect:/view/plantilla";
    }

    // EDITAR
    @GetMapping("/view/bitacoras/edit/{id}")
    public String edit(@PathVariable Long id,
                       Model model)
    {
        Plantilla bitacora = plantillaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plantilla", id));

        model.addAttribute("plantilla", bitacora);

        return "Plantilla/PlantillaForm";
    }

    // ELIMINAR
    @PostMapping("/view/bitacoras/delete/{id}")
    public String delete(@PathVariable Long id,
                         RedirectAttributes ra)
    {
        if (!plantillaRepository.existsById(id)) {
            ra.addFlashAttribute("error", "La plantilla con id " + id + " no existe");
            return "redirect:/view/plantilla";
        }

        plantillaRepository.deleteById(id);

        ra.addFlashAttribute("success",
                "Plantilla eliminada con éxito");

        return "redirect:/view/plantilla";
    }
}