package com.example.textile.service;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.request.LigneCommandeRequest;
import com.example.textile.dto.response.CommandeResponse;
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
class CommandeServiceImplTest {

    @Autowired
    private CommandeService commandeService;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ProduitRepository produitRepository;

    @Test
    void creerCommande_doitReussirEtRetournerUnNumero() {
        Client client = new Client();
        client.setNom("Client Test");
        client.setEmail("test-" + System.currentTimeMillis() + "@example.com");
        client = clientRepository.save(client);

        Produit produit = new Produit();
        produit.setNom("Chemisier");
        produit.setReference("REF-" + System.currentTimeMillis());
        produit = produitRepository.save(produit);

        LigneCommandeRequest ligne = new LigneCommandeRequest();
        ligne.setProduitId(produit.getId());
        ligne.setQuantite(5);

        CommandeRequest request = new CommandeRequest();
        request.setClientId(client.getId());
        request.setLignes(List.of(ligne));

        CommandeResponse response = commandeService.creerCommande(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getNumero()).startsWith("CMD-");
        assertThat(response.getLignes()).hasSize(1);
    }
}