package com.example.textile.repository;

import com.example.textile.entity.Commande;
import com.example.textile.entity.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {
    boolean existsByNumero(String numero);
    List<Commande> findByStatut(StatutCommande statut);
    List<Commande> findByClientId(Long clientId);
}