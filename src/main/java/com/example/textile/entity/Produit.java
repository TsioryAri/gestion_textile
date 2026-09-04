package com.example.textile.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nom;

    @Column(unique = true, nullable = false)
    private String reference;

    private String description;

    @Positive(message = "Le prix doit être positif")
    @Column(name = "prix_unitaire_defaut", precision = 10, scale = 2)
    private BigDecimal prixUnitaireDefaut;
}