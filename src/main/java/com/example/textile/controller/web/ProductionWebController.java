package com.example.textile.controller.web;

import com.example.textile.entity.TypeEtape;
import com.example.textile.exception.BusinessException;
import com.example.textile.exception.ResourceNotFoundException;
import com.example.textile.service.ProductionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.textile.entity.ResultatQualite;

@Controller
@RequestMapping("/commandes/{commandeId}/production")
public class ProductionWebController {

    private final ProductionService productionService;

    public ProductionWebController(ProductionService productionService) {
        this.productionService = productionService;
    }

    @PostMapping("/steps/{step}/start")
    public String demarrerEtape(@PathVariable Long commandeId,
                                @PathVariable TypeEtape step,
                                RedirectAttributes redirectAttributes) {
        try {
            productionService.demarrerEtape(commandeId, step);
            redirectAttributes.addFlashAttribute("succesMessage", "Étape " + step + " démarrée.");
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("erreurTransition", e.getMessage());
        }
        return "redirect:/commandes/" + commandeId;
    }

    @PostMapping("/steps/{step}/complete")
    public String terminerEtape(@PathVariable Long commandeId,
                                @PathVariable TypeEtape step,
                                @RequestParam(required = false) Integer quantiteTraitee,
                                RedirectAttributes redirectAttributes) {
        try {
            productionService.terminerEtape(commandeId, step, quantiteTraitee);
            redirectAttributes.addFlashAttribute("succesMessage", "Étape " + step + " terminée.");
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("erreurTransition", e.getMessage());
        }
        return "redirect:/commandes/" + commandeId;
    }

    @PostMapping("/quality-control")
    public String effectuerControleQualite(@PathVariable Long commandeId,
                                           @RequestParam ResultatQualite resultat,
                                           @RequestParam(required = false) String commentaire,
                                           RedirectAttributes redirectAttributes) {
        try {
            productionService.effectuerControleQualite(commandeId, resultat, commentaire);
            String message = resultat == ResultatQualite.CONFORME
                    ? "Contrôle qualité validé : CONFORME."
                    : "Contrôle qualité effectué : NON CONFORME — la livraison est bloquée.";
            redirectAttributes.addFlashAttribute("succesMessage", message);
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("erreurTransition", e.getMessage());
        }
        return "redirect:/commandes/" + commandeId;
    }
}