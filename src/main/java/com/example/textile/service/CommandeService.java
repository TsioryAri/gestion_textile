package com.example.textile.service;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.response.CommandeResponse;
import com.example.textile.entity.Priorite;
import com.example.textile.entity.StatutCommande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.textile.dto.response.StatistiquesResponse;

import java.time.LocalDate;
import java.util.List;

public interface CommandeService {
    CommandeResponse creerCommande(CommandeRequest request);
    List<CommandeResponse> listerCommandes();
    CommandeResponse obtenirCommande(Long id);
    List<CommandeResponse> listerCommandesEnRetard();
    Page<CommandeResponse> rechercherCommandes(StatutCommande statut, Priorite priorite,
                                               LocalDate dateDebut, LocalDate dateFin, Pageable pageable);
    StatistiquesResponse obtenirStatistiques();
}