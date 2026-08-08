package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.model.Estados;
import com.vitaNova.vitaNova.repository.EstadosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//devuelve un archivo tipo HTML
@Controller
public class EstadosView
{
    //comuniaccion con la base de datos a tra vez del jpa
    @Autowired
    private EstadosRepository estadosRepository;

    //llena la tabla que muetra la info de los usuarios
    @GetMapping("/view/estados")
    public String lista(Model model)
    {
        model.addAttribute("estados", estadosRepository.findAll());
        return "estados/estados";
    }

    @GetMapping("/view/estados/form")
    public String form(Model model)
    {
        model.addAttribute("estados", new Estados());
        return "estados/estadosForm";
    }

    //sirve para guardar la lista
    @PostMapping("/view/estados/save")
    public String save(@ModelAttribute Estados estados, RedirectAttributes ra)
    {
        estadosRepository.save(estados);
        ra.addFlashAttribute("success", "Estado registrado con exito");
        return "redirect:/view/estados";
    }

    //editar estados
    @GetMapping("/view/estados/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra)
    {
        Estados estados = estadosRepository.findById(id).orElse(null);
        if (estados == null) {
            ra.addFlashAttribute("error", "El estado con id " + id + " no existe");
            return "redirect:/view/estados";
        }
        model.addAttribute("estados", estados);
        return "estados/estadosForm";
    }

    //borrar estados
    @PostMapping("/view/estados/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra)
    {
        if (!estadosRepository.existsById(id)) {
            ra.addFlashAttribute("error", "El estado con id " + id + " no existe");
            return "redirect:/view/estados";
        }

        estadosRepository.deleteById(id);
        ra.addFlashAttribute("success", "Estado eliminado con exito");
        return "redirect:/view/estados";
    }
}