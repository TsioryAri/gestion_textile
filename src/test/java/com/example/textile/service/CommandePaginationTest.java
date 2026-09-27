package com.example.textile.service;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.request.LigneCommandeRequest;
import com.example.textile.dto.response.CommandeResponse;
import com.example.textile.entity.Client;
import com.example.textile.entity.Priorite;
import com.example.textile.entity.Produit;
import com.example.textile.entity.StatutCommande;
import com.example.textile.repository.ClientRepository;
import com.example.textile.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CommandePaginationTest {

    @Autowired private CommandeService commandeService;
    @Autowired private ClientRepository clientRepository;
    @Autowired private ProduitRepository produitRepository;

    private Long creerClient() {
        Client client = new Client();
        client.setNom("Client Pagination Test");
        client.setEmail("pagination-" + System.currentTimeMillis() + "@example.com");
        return clientRepository.save(client).getId();
    }

    private Long creerProduit() {
        Produit produit = new Produit();
        produit.setNom("Produit Pagination");
        produit.setReference("REF-PAG-" + System.currentTimeMillis());
        return produitRepository.save(produit).getId();
    }

    private void creerCommandeAvecPriorite(Long clientId, Long produitId, Priorite priorite) {
        LigneCommandeRequest ligne = new LigneCommandeRequest();
        ligne.setProduitId(produitId);
        ligne.setQuantite(5);

        CommandeRequest request = new CommandeRequest();
        request.setClientId(clientId);
        request.setPriorite(priorite);
        request.setLignes(List.of(ligne));

        commandeService.creerCommande(request);
    }

    @Test
    void rechercherSansFiltre_doitRespecterLaTailleDePage() {
        Long clientId = creerClient();
        Long produitId = creerProduit();
        for (int i = 0; i < 3; i++) {
            creerCommandeAvecPriorite(clientId, produitId, Priorite.NORMALE);
        }

        Page<CommandeResponse> page = commandeService.rechercherCommandes(
                null, null, null, null, PageRequest.of(0, 2, Sort.by("dateCommande").descending()));

        assertThat(page.getContent()).hasSizeLessThanOrEqualTo(2);
        assertThat(page.getSize()).isEqualTo(2);
    }

    @Test
    void filtrerParPriorite_doitNeRetournerQueCellesCorrespondantes() {
        Long clientId = creerClient();
        Long produitId = creerProduit();
        creerCommandeAvecPriorite(clientId, produitId, Priorite.TRES_URGENTE);
        creerCommandeAvecPriorite(clientId, produitId, Priorite.NORMALE);

        Page<CommandeResponse> page = commandeService.rechercherCommandes(
                null, Priorite.TRES_URGENTE, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).allMatch(c -> c.getPriorite() == Priorite.TRES_URGENTE);
    }

    @Test
    void filtrerParStatut_doitNeRetournerQueCellesCorrespondantes() {
        Long clientId = creerClient();
        Long produitId = creerProduit();
        creerCommandeAvecPriorite(clientId, produitId, Priorite.NORMALE); // statut EN_ATTENTE par défaut

        Page<CommandeResponse> page = commandeService.rechercherCommandes(
                StatutCommande.EN_ATTENTE, null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).allMatch(c -> c.getStatut() == StatutCommande.EN_ATTENTE);
    }
}