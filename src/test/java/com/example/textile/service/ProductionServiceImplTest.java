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
import com.example.textile.repository.EtapeProductionRepository;
import com.example.textile.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ProductionServiceImplTest {

    @Autowired private CommandeService commandeService;
    @Autowired private ProductionService productionService;
    @Autowired private ClientRepository clientRepository;
    @Autowired private ProduitRepository produitRepository;
    @Autowired private EtapeProductionRepository etapeProductionRepository;

    private Long creerCommandeDeTest() {
        Client client = new Client();
        client.setNom("Client Prod Test");
        client.setEmail("prod-test-" + System.currentTimeMillis() + "@example.com");
        client = clientRepository.save(client);

        Produit produit = new Produit();
        produit.setNom("T-shirt");
        produit.setReference("REF-PROD-" + System.currentTimeMillis());
        produit = produitRepository.save(produit);

        LigneCommandeRequest ligne = new LigneCommandeRequest();
        ligne.setProduitId(produit.getId());
        ligne.setQuantite(20);

        CommandeRequest request = new CommandeRequest();
        request.setClientId(client.getId());
        request.setLignes(List.of(ligne));

        CommandeResponse response = commandeService.creerCommande(request);
        return response.getId();
    }

    @Test
    void creationCommande_doitInitialiserLes5EtapesEnAttente() {
        Long commandeId = creerCommandeDeTest();

        List<EtapeProduction> etapes = etapeProductionRepository.findByCommandeId(commandeId);

        assertThat(etapes).hasSize(5);
        assertThat(etapes).allMatch(e -> e.getStatut().name().equals("EN_ATTENTE"));
    }

    @Test
    void demarrerPremiereEtape_coupe_doitReussir() {
        Long commandeId = creerCommandeDeTest();

        EtapeProduction etape = productionService.demarrerEtape(commandeId, TypeEtape.COUPE);

        assertThat(etape.getStatut().name()).isEqualTo("EN_COURS");
        assertThat(etape.getDateDebut()).isNotNull();
    }

    @Test
    void demarrerCouture_avantQueCoupeSoitTerminee_doitEchouer() {
        Long commandeId = creerCommandeDeTest();
        productionService.demarrerEtape(commandeId, TypeEtape.COUPE);

        assertThatThrownBy(() -> productionService.demarrerEtape(commandeId, TypeEtape.COUTURE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("n'est pas terminée");
    }

    @Test
    void sequenceComplete_coupeTermineePuisCouture_doitReussir() {
        Long commandeId = creerCommandeDeTest();

        productionService.demarrerEtape(commandeId, TypeEtape.COUPE);
        productionService.terminerEtape(commandeId, TypeEtape.COUPE);

        EtapeProduction couture = productionService.demarrerEtape(commandeId, TypeEtape.COUTURE);

        assertThat(couture.getStatut().name()).isEqualTo("EN_COURS");
    }

    @Test
    void terminerEtapeNonDemarree_doitEchouer() {
        Long commandeId = creerCommandeDeTest();

        assertThatThrownBy(() -> productionService.terminerEtape(commandeId, TypeEtape.COUPE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("EN_COURS");
    }
}
