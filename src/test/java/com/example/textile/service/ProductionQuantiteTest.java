package com.example.textile.service;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.request.LigneCommandeRequest;
import com.example.textile.dto.response.CommandeResponse;
import com.example.textile.entity.Client;
import com.example.textile.entity.EtapeProduction;
import com.example.textile.entity.Produit;
import com.example.textile.entity.TypeEtape;
import com.example.textile.exception.BusinessException;
import com.example.textile.repository.ClientRepository;
import com.example.textile.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ProductionQuantiteTest {

    @Autowired private CommandeService commandeService;
    @Autowired private ProductionService productionService;
    @Autowired private ClientRepository clientRepository;
    @Autowired private ProduitRepository produitRepository;

    // Commande de 500 unités, comme l'exemple des consignes
    private Long creerCommande500Unites() {
        Client client = new Client();
        client.setNom("Client Quantite Test");
        client.setEmail("qte-test-" + System.currentTimeMillis() + "@example.com");
        client = clientRepository.save(client);

        Produit produit = new Produit();
        produit.setNom("Chemise");
        produit.setReference("REF-QTE-" + System.currentTimeMillis());
        produit = produitRepository.save(produit);

        LigneCommandeRequest ligne = new LigneCommandeRequest();
        ligne.setProduitId(produit.getId());
        ligne.setQuantite(500);

        CommandeRequest request = new CommandeRequest();
        request.setClientId(client.getId());
        request.setLignes(List.of(ligne));

        CommandeResponse response = commandeService.creerCommande(request);
        return response.getId();
    }

    @Test
    void terminerCoupeAvecQuantiteValide_doitReussir() {
        Long commandeId = creerCommande500Unites();
        productionService.demarrerEtape(commandeId, TypeEtape.COUPE);

        EtapeProduction etape = productionService.terminerEtape(commandeId, TypeEtape.COUPE, 500);

        assertThat(etape.getQuantiteTraitee()).isEqualTo(500);
    }

    @Test
    void terminerCoupeSansQuantite_doitEchouer() {
        Long commandeId = creerCommande500Unites();
        productionService.demarrerEtape(commandeId, TypeEtape.COUPE);

        assertThatThrownBy(() -> productionService.terminerEtape(commandeId, TypeEtape.COUPE, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("obligatoire");
    }

    @Test
    void terminerAvecQuantiteSuperieureACommandee_doitEchouer() {
        Long commandeId = creerCommande500Unites();
        productionService.demarrerEtape(commandeId, TypeEtape.COUPE);

        assertThatThrownBy(() -> productionService.terminerEtape(commandeId, TypeEtape.COUPE, 600))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("dépasse la quantité commandée");
    }

    @Test
    void terminerCoutureAvecQuantiteSuperieureACoupe_doitEchouer() {
        Long commandeId = creerCommande500Unites();
        productionService.demarrerEtape(commandeId, TypeEtape.COUPE);
        productionService.terminerEtape(commandeId, TypeEtape.COUPE, 490); // volontairement < 500
        productionService.demarrerEtape(commandeId, TypeEtape.COUTURE);

        // 500 ne dépasse pas la quantité commandée (500), mais dépasse bien la Coupe (490)
        assertThatThrownBy(() -> productionService.terminerEtape(commandeId, TypeEtape.COUTURE, 500))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ne peut pas dépasser celle de l'étape précédente");
    }

    @Test
    void sequenceRealiste_coupe500_couture480_finition450_doitReussir() {
        // Reproduit l'exemple exact des consignes : 500 -> 480 -> 450
        Long commandeId = creerCommande500Unites();

        productionService.demarrerEtape(commandeId, TypeEtape.COUPE);
        productionService.terminerEtape(commandeId, TypeEtape.COUPE, 500);

        productionService.demarrerEtape(commandeId, TypeEtape.COUTURE);
        productionService.terminerEtape(commandeId, TypeEtape.COUTURE, 480);

        productionService.demarrerEtape(commandeId, TypeEtape.FINITION);
        EtapeProduction finition = productionService.terminerEtape(commandeId, TypeEtape.FINITION, 450);

        assertThat(finition.getQuantiteTraitee()).isEqualTo(450);
    }
}