package com.example.textile.dto.response;

import com.example.textile.entity.Priorite;
import com.example.textile.entity.StatutCommande;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesResponse {
    private long totalCommandes;
    private Map<StatutCommande, Long> commandesParStatut;
    private Map<Priorite, Long> commandesParPriorite;
    private long commandesEnRetard;
    private double tauxRetardPourcentage;
}