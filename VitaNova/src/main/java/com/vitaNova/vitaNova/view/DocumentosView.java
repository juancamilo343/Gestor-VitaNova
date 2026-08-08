package com.vitaNova.vitaNova.view;

import com.vitaNova.vitaNova.repository.DocumentosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DocumentosView {
    @Autowired
    private DocumentosRepository documentosRepository;

    @GetMapping("/view/documentos")
    public String lista(Model model) {
        model.addAttribute("documentos", documentosRepository.findAll());
        return "documentos/documentos";
    }

    @GetMapping("/view/documentos/form")
    public String form(Model model) {
        model.addAttribute("documentos", new com.vitaNova.vitaNova.model.Documentos());
        return "documentos/documentosForm";
    }

    @PostMapping("/view/documentos/save")
    public String save(@ModelAttribute com.vitaNova.vitaNova.model.Documentos documentos, RedirectAttributes ra) {
        documentosRepository.save(documentos);
        ra.addFlashAttribute("success", "documentos registrado exitosamente");
        return "redirect:/view/documentos";
    }

    @GetMapping("/view/documentos/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra) {
        com.vitaNova.vitaNova.model.Documentos documentos = documentosRepository.findById(id).orElse(null);
        if (documentos == null) {
            ra.addFlashAttribute("error", "El documento con id " + id + " no existe");
            return "redirect:/view/documentos";
        }
        model.addAttribute("documentos", documentos);
        return "documentos/documentosForm";
    }

    @PostMapping("/view/documentos/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        if (!documentosRepository.existsById(id)) {
            ra.addFlashAttribute("error", "El documento con id " + id + " no existe");
            return "redirect:/view/documentos";
        }

        documentosRepository.deleteById(id);
        ra.addFlashAttribute("success", "Documentos eliminado exitosamente");
        return "redirect:/view/documentos";
    }
}