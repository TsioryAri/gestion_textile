package com.example.textile.service;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.request.LigneCommandeRequest;
import com.example.textile.entity.Client;
import com.example.textile.entity.Commande;
import com.example.textile.entity.Produit;
import com.example.textile.entity.ResultatQualite;
import com.example.textile.entity.StatutCommande;
import com.example.textile.entity.TypeEtape;
import com.example.textile.repository.ClientRepository;
import com.example.textile.repository.CommandeRepository;
import com.example.textile.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CommandeStatutTest {

    @Autowired private CommandeService commandeService;
    @Autowired private ProductionService productionService;
    @Autowired private ClientRepository clientRepository;
    @Autowired private ProduitRepository produitRepository;
    @Autowired private CommandeRepository commandeRepository;

    private Long creerCommande(int quantite, LocalDate datePrevue) {
        Client client = new Client();
        client.setNom("Client Statut Test");
        client.setEmail("statut-" + System.nanoTime() + "@example.com");
        client = clientRepository.save(client);

        Produit produit = new Produit();
        produit.setNom("Produit Statut");
        produit.setReference("REF-STATUT-" + System.nanoTime());
        produit = produitRepository.save(produit);

        LigneCommandeRequest ligne = new LigneCommandeRequest();
        ligne.setProduitId(produit.getId());
        ligne.setQuantite(quantite);

        CommandeRequest request = new CommandeRequest();
        request.setClientId(client.getId());
        request.setDatePrevueLivraison(datePrevue);
        request.setLignes(List.of(ligne));

        return commandeService.creerCommande(request).getId();
    }

    private StatutCommande statutDe(Long commandeId) {
        return commandeRepository.findById(commandeId).orElseThrow().getStatut();
    }

    private void avancerJusquaLivraisonTerminee(Long id, int quantite) {
        productionService.demarrerEtape(id, TypeEtape.COUPE);
        productionService.terminerEtape(id, TypeEtape.COUPE, quantite);
        productionService.demarrerEtape(id, TypeEtape.COUTURE);
        productionService.terminerEtape(id, TypeEtape.COUTURE, quantite);
        productionService.demarrerEtape(id, TypeEtape.FINITION);
        productionService.terminerEtape(id, TypeEtape.FINITION, quantite);
        productionService.demarrerEtape(id, TypeEtape.CONTROLE_QUALITE);
        productionService.effectuerControleQualite(id, ResultatQualite.CONFORME, "RAS");
        productionService.demarrerEtape(id, TypeEtape.LIVRAISON);
        productionService.terminerEtape(id, TypeEtape.LIVRAISON, null);
    }

    @Test
    void statutCommande_doitSuivreLeWorkflowJusquALivraison() {
        Long id = creerCommande(10, null);
        assertThat(statutDe(id)).isEqualTo(StatutCommande.EN_ATTENTE);

        productionService.demarrerEtape(id, TypeEtape.COUPE);
        assertThat(statutDe(id)).isEqualTo(StatutCommande.EN_PRODUCTION);

        productionService.terminerEtape(id, TypeEtape.COUPE, 10);
        productionService.demarrerEtape(id, TypeEtape.COUTURE);
        productionService.terminerEtape(id, TypeEtape.COUTURE, 10);
        productionService.demarrerEtape(id, TypeEtape.FINITION);
        productionService.terminerEtape(id, TypeEtape.FINITION, 10);

        productionService.demarrerEtape(id, TypeEtape.CONTROLE_QUALITE);
        assertThat(statutDe(id)).isEqualTo(StatutCommande.CONTROLE_QUALITE);

        productionService.effectuerControleQualite(id, ResultatQualite.CONFORME, "RAS");
        productionService.demarrerEtape(id, TypeEtape.LIVRAISON);
        productionService.terminerEtape(id, TypeEtape.LIVRAISON, null);
        assertThat(statutDe(id)).isEqualTo(StatutCommande.LIVREE);
    }

    @Test
    void commandeLivree_neDoitPlusEtreListeeEnRetard() {
        Long id = creerCommande(10, LocalDate.now().minusDays(5));
        List<StatutCommande> exclus = List.of(StatutCommande.LIVREE, StatutCommande.ANNULEE);

        List<Commande> avant = commandeRepository
                .findByDatePrevueLivraisonBeforeAndStatutNotIn(LocalDate.now(), exclus);
        assertThat(avant).anyMatch(c -> c.getId().equals(id));

        avancerJusquaLivraisonTerminee(id, 10);

        List<Commande> apres = commandeRepository
                .findByDatePrevueLivraisonBeforeAndStatutNotIn(LocalDate.now(), exclus);
        assertThat(apres).noneMatch(c -> c.getId().equals(id));
    }
}
