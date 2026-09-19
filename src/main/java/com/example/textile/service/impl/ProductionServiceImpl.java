package com.example.textile.service.impl;

import com.example.textile.entity.*;
import com.example.textile.exception.BusinessException;
import com.example.textile.exception.ResourceNotFoundException;
import com.example.textile.repository.EtapeProductionRepository;
import com.example.textile.service.ProductionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductionServiceImpl implements ProductionService {

    private static final List<TypeEtape> ETAPES_AVEC_QUANTITE =
            List.of(TypeEtape.COUPE, TypeEtape.COUTURE, TypeEtape.FINITION);

    private final EtapeProductionRepository etapeProductionRepository;

    public ProductionServiceImpl(EtapeProductionRepository etapeProductionRepository) {
        this.etapeProductionRepository = etapeProductionRepository;
    }

    @Override
    public List<EtapeProduction> initialiserEtapes(Commande commande) {
        List<EtapeProduction> etapes = new ArrayList<>();
        for (TypeEtape type : TypeEtape.values()) {
            EtapeProduction etape = new EtapeProduction();
            etape.setCommande(commande);
            etape.setTypeEtape(type);
            etape.setStatut(StatutEtape.EN_ATTENTE);
            etapes.add(etape);
        }
        return etapes;
    }

    @Override
    @Transactional
    public EtapeProduction demarrerEtape(Long commandeId, TypeEtape type) {
        EtapeProduction etape = obtenirEtape(commandeId, type);

        if (etape.getStatut() != StatutEtape.EN_ATTENTE) {
            throw new BusinessException(
                    "Impossible de démarrer l'étape " + type + " : statut actuel = " + etape.getStatut());
        }

        if (type.ordinal() > 0) {
            TypeEtape typePrecedent = TypeEtape.values()[type.ordinal() - 1];
            EtapeProduction etapePrecedente = obtenirEtape(commandeId, typePrecedent);

            if (etapePrecedente.getStatut() != StatutEtape.TERMINEE) {
                throw new BusinessException(
                        "Impossible de démarrer " + type + " : l'étape précédente (" + typePrecedent
                                + ") n'est pas terminée (statut actuel = " + etapePrecedente.getStatut() + ")");
            }

            // Règle spécifique : la livraison est bloquée si le contrôle qualité n'est pas conforme
            if (type == TypeEtape.LIVRAISON && etapePrecedente.getResultatQualite() != ResultatQualite.CONFORME) {
                throw new BusinessException(
                        "Livraison bloquée : le contrôle qualité n'est pas conforme (résultat = "
                                + etapePrecedente.getResultatQualite() + ")");
            }
        }

        etape.setStatut(StatutEtape.EN_COURS);
        etape.setDateDebut(LocalDateTime.now());
        return etapeProductionRepository.save(etape);
    }

    @Override
    @Transactional
    public EtapeProduction terminerEtape(Long commandeId, TypeEtape type, Integer quantiteTraitee) {
        if (type == TypeEtape.CONTROLE_QUALITE) {
            throw new BusinessException(
                    "Le contrôle qualité doit être validé via l'endpoint dédié (quality-control), pas via /complete");
        }

        EtapeProduction etape = obtenirEtape(commandeId, type);

        if (etape.getStatut() != StatutEtape.EN_COURS) {
            throw new BusinessException(
                    "Impossible de terminer l'étape " + type + " : statut actuel = " + etape.getStatut()
                            + " (elle doit être EN_COURS)");
        }

        if (ETAPES_AVEC_QUANTITE.contains(type)) {
            validerQuantiteTraitee(etape, quantiteTraitee);
            etape.setQuantiteTraitee(quantiteTraitee);
        }

        etape.setStatut(StatutEtape.TERMINEE);
        etape.setDateFinReelle(LocalDateTime.now());
        return etapeProductionRepository.save(etape);
    }

    @Override
    @Transactional
    public EtapeProduction effectuerControleQualite(Long commandeId, ResultatQualite resultat, String commentaire) {
        EtapeProduction etape = obtenirEtape(commandeId, TypeEtape.CONTROLE_QUALITE);

        if (etape.getStatut() != StatutEtape.EN_COURS) {
            throw new BusinessException(
                    "Impossible de valider le contrôle qualité : statut actuel = " + etape.getStatut()
                            + " (l'étape doit être EN_COURS ; démarrez-la d'abord)");
        }

        etape.setResultatQualite(resultat);
        etape.setCommentaire(commentaire);
        etape.setStatut(StatutEtape.TERMINEE);
        etape.setDateFinReelle(LocalDateTime.now());
        return etapeProductionRepository.save(etape);
    }

    private void validerQuantiteTraitee(EtapeProduction etape, Integer quantiteTraitee) {
        TypeEtape type = etape.getTypeEtape();

        if (quantiteTraitee == null || quantiteTraitee <= 0) {
            throw new BusinessException(
                    "La quantité traitée est obligatoire et doit être positive pour l'étape " + type);
        }

        int quantiteCommandee = etape.getCommande().getLignesCommande().stream()
                .mapToInt(LigneCommande::getQuantite)
                .sum();

        if (quantiteTraitee > quantiteCommandee) {
            throw new BusinessException(
                    "La quantité traitée (" + quantiteTraitee + ") dépasse la quantité commandée ("
                            + quantiteCommandee + ")");
        }

        int indexActuel = ETAPES_AVEC_QUANTITE.indexOf(type);
        if (indexActuel > 0) {
            TypeEtape typePrecedent = ETAPES_AVEC_QUANTITE.get(indexActuel - 1);
            EtapeProduction etapePrecedente = obtenirEtape(etape.getCommande().getId(), typePrecedent);
            Integer quantitePrecedente = etapePrecedente.getQuantiteTraitee();

            if (quantitePrecedente == null || quantiteTraitee > quantitePrecedente) {
                throw new BusinessException(
                        "La quantité traitée pour " + type + " (" + quantiteTraitee
                                + ") ne peut pas dépasser celle de l'étape précédente " + typePrecedent
                                + " (" + (quantitePrecedente != null ? quantitePrecedente : 0) + ")");
            }
        }
    }

    private EtapeProduction obtenirEtape(Long commandeId, TypeEtape type) {
        return etapeProductionRepository.findByCommandeIdAndTypeEtape(commandeId, type)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Étape " + type + " introuvable pour la commande " + commandeId));
    }
}