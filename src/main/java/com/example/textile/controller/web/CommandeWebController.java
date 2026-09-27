package com.example.textile.controller.web;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.request.LigneCommandeRequest;
import com.example.textile.entity.Priorite;
import com.example.textile.exception.BusinessException;
import com.example.textile.exception.ResourceNotFoundException;
import com.example.textile.repository.ClientRepository;
import com.example.textile.repository.EtapeProductionRepository;
import com.example.textile.repository.ProduitRepository;
import com.example.textile.service.CommandeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.textile.dto.response.CommandeResponse;
import com.example.textile.entity.StatutCommande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

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
    private final EtapeProductionRepository etapeProductionRepository;
    private final Validator validator;

    public CommandeWebController(CommandeService commandeService,
                                 ClientRepository clientRepository,
                                 ProduitRepository produitRepository,
                                 EtapeProductionRepository etapeProductionRepository,
                                 Validator validator) {
        this.commandeService = commandeService;
        this.clientRepository = clientRepository;
        this.produitRepository = produitRepository;
        this.etapeProductionRepository = etapeProductionRepository;
        this.validator = validator;
    }

    @GetMapping
    public String listerCommandes(
            @RequestParam(required = false) StatutCommande status,
            @RequestParam(required = false) Priorite priorite,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @PageableDefault(size = 5, sort = "dateCommande", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        Page<CommandeResponse> resultats = commandeService.rechercherCommandes(status, priorite, dateDebut, dateFin, pageable);

        model.addAttribute("page", resultats);
        model.addAttribute("statutSelectionne", status);
        model.addAttribute("prioriteSelectionnee", priorite);
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        model.addAttribute("statuts", StatutCommande.values());
        model.addAttribute("priorites", Priorite.values());

        return "commandes/liste";
    }

    @GetMapping("/{id}")
    public String detailCommande(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("commande", commandeService.obtenirCommande(id));
            model.addAttribute("etapes", etapeProductionRepository.findByCommandeId(id));
            return "commandes/detail";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("erreurTransition", e.getMessage());
            return "redirect:/commandes";
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/nouvelle")
    public String afficherFormulaire(Model model) {
        model.addAttribute("commandeRequest", nouvelleCommandeVide());
        chargerListesDeroulantes(model);
        return "commandes/formulaire";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public String creerCommande(@ModelAttribute CommandeRequest commandeRequest,
                                BindingResult bindingResult,
                                Model model) {
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