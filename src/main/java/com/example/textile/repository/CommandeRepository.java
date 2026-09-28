package com.example.textile.repository;

import com.example.textile.entity.Commande;
import com.example.textile.entity.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long>, JpaSpecificationExecutor<Commande> {
    boolean existsByNumero(String numero);
    List<Commande> findByStatut(StatutCommande statut);
    List<Commande> findByClientId(Long clientId);
    List<Commande> findByDatePrevueLivraisonBeforeAndStatutNotIn(LocalDate date, List<StatutCommande> statutsExclus);
    long countByDatePrevueLivraisonBeforeAndStatutNotIn(LocalDate date, List<StatutCommande> statutsExclus);
}