package com.example.textile.repository.specification;

import com.example.textile.entity.Commande;
import com.example.textile.entity.Priorite;
import com.example.textile.entity.StatutCommande;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class CommandeSpecifications {

    private CommandeSpecifications() {
        // classe utilitaire, non instanciable
    }

    public static Specification<Commande> avecStatut(StatutCommande statut) {
        return (root, query, cb) -> statut == null ? null : cb.equal(root.get("statut"), statut);
    }

    public static Specification<Commande> avecPriorite(Priorite priorite) {
        return (root, query, cb) -> priorite == null ? null : cb.equal(root.get("priorite"), priorite);
    }

    public static Specification<Commande> dateCommandeApres(LocalDate dateDebut) {
        return (root, query, cb) -> dateDebut == null ? null : cb.greaterThanOrEqualTo(root.get("dateCommande"), dateDebut);
    }

    public static Specification<Commande> dateCommandeAvant(LocalDate dateFin) {
        return (root, query, cb) -> dateFin == null ? null : cb.lessThanOrEqualTo(root.get("dateCommande"), dateFin);
    }
}