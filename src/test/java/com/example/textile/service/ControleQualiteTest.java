package com.example.textile.service;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.request.LigneCommandeRequest;
import com.example.textile.dto.response.CommandeResponse;
import com.example.textile.entity.Client;
import com.example.textile.entity.EtapeProduction;
import com.example.textile.entity.Produit;
import com.example.textile.entity.ResultatQualite;
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
class ControleQualiteTest {

    @Autowired private CommandeService commandeService;
    @Autowired private ProductionService productionService;
    @Autowired private ClientRepository clientRepository;
    @Autowired private ProduitRepository produitRepository;

    private Long creerCommandeDeTest(int quantite) {
        Client client = new Client();
        client.setNom("Client CQ Test");
        client.setEmail("cq-test-" + System.currentTimeMillis() + "@example.com");
        client = clientRepository.save(client);

        Produit produit = new Produit();
        produit.setNom("Pantalon");
        produit.setReference("REF-CQ-" + System.currentTimeMillis());
        produit = produitRepository.save(produit);

        LigneCommandeRequest ligne = new LigneCommandeRequest();
        ligne.setProduitId(produit.getId());
        ligne.setQuantite(quantite);

        CommandeRequest request = new CommandeRequest();
        request.setClientId(client.getId());
        request.setLignes(List.of(ligne));

        CommandeResponse response = commandeService.creerCommande(request);
        return response.getId();
    }

    private void avancerJusquaControleQualite(Long commandeId, int quantite) {
        productionService.demarrerEtape(commandeId, TypeEtape.COUPE);
        productionService.terminerEtape(commandeId, TypeEtape.COUPE, quantite);
        productionService.demarrerEtape(commandeId, TypeEtape.COUTURE);
        productionService.terminerEtape(commandeId, TypeEtape.COUTURE, quantite);
        productionService.demarrerEtape(commandeId, TypeEtape.FINITION);
        productionService.terminerEtape(commandeId, TypeEtape.FINITION, quantite);
        productionService.demarrerEtape(commandeId, TypeEtape.CONTROLE_QUALITE);
    }

    @Test
    void controleQualiteConforme_doitPermettreLaLivraison() {
        Long commandeId = creerCommandeDeTest(50);
        avancerJusquaControleQualite(commandeId, 50);

        EtapeProduction controle = productionService.effectuerControleQualite(
                commandeId, ResultatQualite.CONFORME, "RAS");
        assertThat(controle.getResultatQualite()).isEqualTo(ResultatQualite.CONFORME);

        // La livraison doit pouvoir démarrer normalement
        EtapeProduction livraison = productionService.demarrerEtape(commandeId, TypeEtape.LIVRAISON);
        assertThat(livraison.getStatut().name()).isEqualTo("EN_COURS");
    }

    @Test
    void controleQualiteNonConforme_doitBloquerLaLivraison() {
        Long commandeId = creerCommandeDeTest(50);
        avancerJusquaControleQualite(commandeId, 50);

        productionService.effectuerControleQualite(
                commandeId, ResultatQualite.NON_CONFORME, "Défauts de couture détectés");

        assertThatThrownBy(() -> productionService.demarrerEtape(commandeId, TypeEtape.LIVRAISON))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Livraison bloquée");
    }

    @Test
    void validerControleQualiteAvantDemarrage_doitEchouer() {
        Long commandeId = creerCommandeDeTest(50);
        // On ne démarre PAS le contrôle qualité (reste EN_ATTENTE)

        assertThatThrownBy(() -> productionService.effectuerControleQualite(
                commandeId, ResultatQualite.CONFORME, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("EN_COURS");
    }

    @Test
    void terminerControleQualiteViaEndpointGenerique_doitEchouer() {
        Long commandeId = creerCommandeDeTest(50);
        avancerJusquaControleQualite(commandeId, 50);

        assertThatThrownBy(() -> productionService.terminerEtape(commandeId, TypeEtape.CONTROLE_QUALITE, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("endpoint dédié");
    }
}