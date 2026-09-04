package com.example.textile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "etapes_production")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EtapeProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_etape", nullable = false)
    private TypeEtape typeEtape;

    @Enumerated(EnumType.STRING)
    private StatutEtape statut = StatutEtape.EN_ATTENTE;

    private String responsable;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin_prevue")
    private LocalDateTime dateFinPrevue;

    @Column(name = "date_fin_reelle")
    private LocalDateTime dateFinReelle;

    private String commentaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultat_qualite")
    private ResultatQualite resultatQualite; // rempli uniquement quand typeEtape = CONTROLE_QUALITE
}