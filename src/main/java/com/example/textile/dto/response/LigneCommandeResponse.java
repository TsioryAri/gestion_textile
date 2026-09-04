package com.example.textile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommandeResponse {
    private Long id;
    private String produitNom;
    private Integer quantite;
    private String taille;
    private String couleur;
    private BigDecimal prixUnitaire;
    private Integer quantiteProduite;
}