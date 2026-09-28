package com.example.textile.service.impl;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.request.LigneCommandeRequest;
import com.example.textile.dto.response.CommandeResponse;
import com.example.textile.dto.response.StatistiquesResponse;
import com.example.textile.entity.*;
import com.example.textile.exception.ResourceNotFoundException;
import com.example.textile.mapper.CommandeMapper;
import com.example.textile.repository.ClientRepository;
import com.example.textile.repository.CommandeRepository;
import com.example.textile.repository.ProduitRepository;
import com.example.textile.repository.specification.CommandeSpecifications;
import com.example.textile.service.CommandeService;
import com.example.textile.service.ProductionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class CommandeServiceImpl implements CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;
    private final CommandeMapper commandeMapper;
    private final ProductionService productionService;

    public CommandeServiceImpl(CommandeRepository commandeRepository,
                                ClientRepository clientRepository,
                                ProduitRepository produitRepository,
                                CommandeMapper commandeMapper,
                                ProductionService productionService) {
        this.commandeRepository = commandeRepository;
        this.clientRepository = clientRepository;
        this.produitRepository = produitRepository;
        this.commandeMapper = commandeMapper;
        this.productionService = productionService;
    }

    @Override
    @Transactional
    public CommandeResponse creerCommande(CommandeRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client introuvable avec l'id " + request.getClientId()));

        Commande commande = new Commande();
        commande.setNumero(genererNumeroCommande());
        commande.setClient(client);
        commande.setDatePrevueLivraison(request.getDatePrevueLivraison());
        commande.setPriorite(request.getPriorite() != null ? request.getPriorite() : Priorite.NORMALE);
        commande.setStatut(StatutCommande.EN_ATTENTE);

        List<LigneCommande> lignes = construireLignes(request.getLignes(), commande);
        commande.setLignesCommande(lignes);
        commande.setEtapesProduction(productionService.initialiserEtapes(commande));

        Commande commandeSauvegardee = commandeRepository.save(commande);

        return commandeMapper.toResponse(commandeSauvegardee);
    }

    @Override
    public List<CommandeResponse> listerCommandes() {
        return commandeRepository.findAll().stream()
                .map(commandeMapper::toResponse)
                .toList();
    }

    @Override
    public CommandeResponse obtenirCommande(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec l'id " + id));
        return commandeMapper.toResponse(commande);
    }

    @Override
    public List<CommandeResponse> listerCommandesEnRetard() {
        List<StatutCommande> statutsExclus = List.of(StatutCommande.LIVREE, StatutCommande.ANNULEE);
        return commandeRepository.findByDatePrevueLivraisonBeforeAndStatutNotIn(LocalDate.now(), statutsExclus)
                .stream()
                .map(commandeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommandeResponse> rechercherCommandes(StatutCommande statut, Priorite priorite,
                                                        LocalDate dateDebut, LocalDate dateFin, Pageable pageable) {
        Specification<Commande> spec = Specification
                .where(CommandeSpecifications.avecStatut(statut))
                .and(CommandeSpecifications.avecPriorite(priorite))
                .and(CommandeSpecifications.dateCommandeApres(dateDebut))
                .and(CommandeSpecifications.dateCommandeAvant(dateFin));

        return commandeRepository.findAll(spec, pageable)
                .map(commandeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StatistiquesResponse obtenirStatistiques() {
        long total = commandeRepository.count();

        Map<StatutCommande, Long> parStatut = new EnumMap<>(StatutCommande.class);
        for (StatutCommande s : StatutCommande.values()) {
            parStatut.put(s, commandeRepository.count(CommandeSpecifications.avecStatut(s)));
        }

        Map<Priorite, Long> parPriorite = new EnumMap<>(Priorite.class);
        for (Priorite p : Priorite.values()) {
            parPriorite.put(p, commandeRepository.count(CommandeSpecifications.avecPriorite(p)));
        }

        List<StatutCommande> statutsExclus = List.of(StatutCommande.LIVREE, StatutCommande.ANNULEE);
        long enRetard = commandeRepository.countByDatePrevueLivraisonBeforeAndStatutNotIn(LocalDate.now(), statutsExclus);

        double tauxRetard = total > 0 ? (enRetard * 100.0 / total) : 0.0;

        return new StatistiquesResponse(total, parStatut, parPriorite, enRetard, tauxRetard);
    }

    private List<LigneCommande> construireLignes(List<LigneCommandeRequest> lignesRequest, Commande commande) {
        List<LigneCommande> lignes = new ArrayList<>();
        for (LigneCommandeRequest ligneRequest : lignesRequest) {
            Produit produit = produitRepository.findById(ligneRequest.getProduitId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Produit introuvable avec l'id " + ligneRequest.getProduitId()));

            LigneCommande ligne = new LigneCommande();
            ligne.setCommande(commande);
            ligne.setProduit(produit);
            ligne.setQuantite(ligneRequest.getQuantite());
            ligne.setTaille(ligneRequest.getTaille());
            ligne.setCouleur(ligneRequest.getCouleur());
            ligne.setPrixUnitaire(
                    ligneRequest.getPrixUnitaire() != null
                            ? ligneRequest.getPrixUnitaire()
                            : produit.getPrixUnitaireDefaut()
            );
            lignes.add(ligne);
        }
        return lignes;
    }

    private String genererNumeroCommande() {
        return "CMD-" + Instant.now().toEpochMilli();
    }
}
