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
        }

        etape.setStatut(StatutEtape.EN_COURS);
        etape.setDateDebut(LocalDateTime.now());
        return etapeProductionRepository.save(etape);
    }

    @Override
    @Transactional
    public EtapeProduction terminerEtape(Long commandeId, TypeEtape type) {
        EtapeProduction etape = obtenirEtape(commandeId, type);

        if (etape.getStatut() != StatutEtape.EN_COURS) {
            throw new BusinessException(
                    "Impossible de terminer l'étape " + type + " : statut actuel = " + etape.getStatut()
                            + " (elle doit être EN_COURS)");
        }

        etape.setStatut(StatutEtape.TERMINEE);
        etape.setDateFinReelle(LocalDateTime.now());
        return etapeProductionRepository.save(etape);
    }

    private EtapeProduction obtenirEtape(Long commandeId, TypeEtape type) {
        return etapeProductionRepository.findByCommandeIdAndTypeEtape(commandeId, type)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Étape " + type + " introuvable pour la commande " + commandeId));
    }
}