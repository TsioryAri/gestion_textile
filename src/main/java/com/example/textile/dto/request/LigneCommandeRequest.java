package com.example.textile.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LigneCommandeRequest {

    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;

    @NotNull
    @Positive(message = "La quantité doit être positive")
    private Integer quantite;

    private String taille;
    private String couleur;

    @Positive(message = "Le prix unitaire doit être positif")
    private java.math.BigDecimal prixUnitaire;
}