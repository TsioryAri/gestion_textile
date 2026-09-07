package com.example.textile.controller.web;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.request.LigneCommandeRequest;
import com.example.textile.entity.Priorite;
import com.example.textile.exception.BusinessException;
import com.example.textile.exception.ResourceNotFoundException;
import com.example.textile.repository.ClientRepository;
import com.example.textile.repository.ProduitRepository;
import com.example.textile.service.CommandeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/commandes")
public class CommandeWebController {

    private static final int NB_LIGNES_FORMULAIRE = 3;

    private final CommandeService commandeService;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;
    private final Validator validator;

    public CommandeWebController(CommandeService commandeService,
                                 ClientRepository clientRepository,
                                 ProduitRepository produitRepository,
                                 Validator validator) {
        this.commandeService = commandeService;
        this.clientRepository = clientRepository;
        this.produitRepository = produitRepository;
        this.validator = validator;
    }

    @GetMapping
    public String listerCommandes(Model model) {
        model.addAttribute("commandes", commandeService.listerCommandes());
        return "commandes/liste";
    }

    @GetMapping("/nouvelle")
    public String afficherFormulaire(Model model) {
        model.addAttribute("commandeRequest", nouvelleCommandeVide());
        chargerListesDeroulantes(model);
        return "commandes/formulaire";
    }

    @PostMapping
    public String creerCommande(@ModelAttribute CommandeRequest commandeRequest,
                                BindingResult bindingResult,
                                Model model) {
        // Le formulaire propose 3 emplacements ; on retire ceux laissés vides
        List<LigneCommandeRequest> lignesRemplies = commandeRequest.getLignes().stream()
                .filter(l -> l.getProduitId() != null && l.getQuantite() != null)
                .collect(Collectors.toList());
        commandeRequest.setLignes(lignesRemplies);

        validator.validate(commandeRequest, bindingResult);

        if (bindingResult.hasErrors()) {
            chargerListesDeroulantes(model);
            return "commandes/formulaire";
        }

        try {
            commandeService.creerCommande(commandeRequest);
        } catch (ResourceNotFoundException | BusinessException e) {
            model.addAttribute("erreurMetier", e.getMessage());
            chargerListesDeroulantes(model);
            return "commandes/formulaire";
        }

        return "redirect:/commandes";
    }

    private CommandeRequest nouvelleCommandeVide() {
        CommandeRequest request = new CommandeRequest();
        List<LigneCommandeRequest> lignes = new ArrayList<>();
        for (int i = 0; i < NB_LIGNES_FORMULAIRE; i++) {
            lignes.add(new LigneCommandeRequest());
        }
        request.setLignes(lignes);
        request.setPriorite(Priorite.NORMALE);
        return request;
    }

    private void chargerListesDeroulantes(Model model) {
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("produits", produitRepository.findAll());
        model.addAttribute("priorites", Priorite.values());
    }
}