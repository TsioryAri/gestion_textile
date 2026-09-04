package com.example.textile.dto.response;

import com.example.textile.entity.Priorite;
import com.example.textile.entity.StatutCommande;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommandeResponse {
    private Long id;
    private String numero;
    private ClientResponse client;
    private LocalDate dateCommande;
    private LocalDate datePrevueLivraison;
    private Priorite priorite;
    private StatutCommande statut;
    private List<LigneCommandeResponse> lignes;
}