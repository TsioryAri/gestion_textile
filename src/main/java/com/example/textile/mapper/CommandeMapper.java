package com.example.textile.mapper;

import com.example.textile.dto.response.ClientResponse;
import com.example.textile.dto.response.CommandeResponse;
import com.example.textile.dto.response.LigneCommandeResponse;
import com.example.textile.entity.Client;
import com.example.textile.entity.Commande;
import com.example.textile.entity.LigneCommande;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandeMapper {

    public CommandeResponse toResponse(Commande commande) {
        CommandeResponse response = new CommandeResponse();
        response.setId(commande.getId());
        response.setNumero(commande.getNumero());
        response.setClient(toClientResponse(commande.getClient()));
        response.setDateCommande(commande.getDateCommande());
        response.setDatePrevueLivraison(commande.getDatePrevueLivraison());
        response.setPriorite(commande.getPriorite());
        response.setStatut(commande.getStatut());
        response.setLignes(toLigneResponseList(commande.getLignesCommande()));
        return response;
    }

    private ClientResponse toClientResponse(Client client) {
        return new ClientResponse(client.getId(), client.getNom(), client.getEmail());
    }

    private List<LigneCommandeResponse> toLigneResponseList(List<LigneCommande> lignes) {
        return lignes.stream()
                .map(this::toLigneResponse)
                .toList();
    }

    private LigneCommandeResponse toLigneResponse(LigneCommande ligne) {
        return new LigneCommandeResponse(
                ligne.getId(),
                ligne.getProduit().getNom(),
                ligne.getQuantite(),
                ligne.getTaille(),
                ligne.getCouleur(),
                ligne.getPrixUnitaire(),
                ligne.getQuantiteProduite()
        );
    }
}