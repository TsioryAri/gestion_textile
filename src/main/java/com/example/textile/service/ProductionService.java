package com.example.textile.service;

import com.example.textile.entity.Commande;
import com.example.textile.entity.EtapeProduction;
import com.example.textile.entity.TypeEtape;

import java.util.List;

public interface ProductionService {
    List<EtapeProduction> initialiserEtapes(Commande commande);
    EtapeProduction demarrerEtape(Long commandeId, TypeEtape type);
    EtapeProduction terminerEtape(Long commandeId, TypeEtape type);
}