package com.example.textile.service.impl;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.request.LigneCommandeRequest;
import com.example.textile.dto.response.CommandeResponse;
import com.example.textile.entity.*;
import com.example.textile.exception.ResourceNotFoundException;
import com.example.textile.mapper.CommandeMapper;
import com.example.textile.repository.ClientRepository;
import com.example.textile.repository.CommandeRepository;
import com.example.textile.repository.ProduitRepository;
import com.example.textile.service.CommandeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommandeServiceImpl implements CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;
    private final CommandeMapper commandeMapper;

    public CommandeServiceImpl(CommandeRepository commandeRepository,
                               ClientRepository clientRepository,
                               ProduitRepository produitRepository,
                               CommandeMapper commandeMapper) {
        this.commandeRepository = commandeRepository;
        this.clientRepository = clientRepository;
        this.produitRepository = produitRepository;
        this.commandeMapper = commandeMapper;
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

        Commande commandeSauvegardee = commandeRepository.save(commande);

        return commandeMapper.toResponse(commandeSauvegardee);
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