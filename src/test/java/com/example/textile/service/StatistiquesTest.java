package com.example.textile.service;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.request.LigneCommandeRequest;
import com.example.textile.dto.response.StatistiquesResponse;
import com.example.textile.entity.Client;
import com.example.textile.entity.Produit;
import com.example.textile.repository.ClientRepository;
import com.example.textile.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StatistiquesTest {

    @Autowired private CommandeService commandeService;
    @Autowired private ClientRepository clientRepository;
    @Autowired private ProduitRepository produitRepository;

    @Test
    void obtenirStatistiques_doitRefleterLesCommandesExistantes() {
        Client client = new Client();
        client.setNom("Client Stats Test");
        client.setEmail("stats-" + System.currentTimeMillis() + "@example.com");
        client = clientRepository.save(client);

        Produit produit = new Produit();
        produit.setNom("Produit Stats");
        produit.setReference("REF-STATS-" + System.currentTimeMillis());
        produit = produitRepository.save(produit);

        LigneCommandeRequest ligne = new LigneCommandeRequest();
        ligne.setProduitId(produit.getId());
        ligne.setQuantite(5);

        CommandeRequest request = new CommandeRequest();
        request.setClientId(client.getId());
        request.setLignes(List.of(ligne));
        commandeService.creerCommande(request);

        StatistiquesResponse stats = commandeService.obtenirStatistiques();

        assertThat(stats.getTotalCommandes()).isGreaterThan(0);
        assertThat(stats.getCommandesParStatut()).containsKey(com.example.textile.entity.StatutCommande.EN_ATTENTE);
        assertThat(stats.getTauxRetardPourcentage()).isBetween(0.0, 100.0);
    }
}