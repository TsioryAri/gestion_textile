package com.example.textile.repository;

import com.example.textile.entity.EtapeProduction;
import com.example.textile.entity.TypeEtape;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtapeProductionRepository extends JpaRepository<EtapeProduction, Long> {
    List<EtapeProduction> findByCommandeId(Long commandeId);
    Optional<EtapeProduction> findByCommandeIdAndTypeEtape(Long commandeId, TypeEtape typeEtape);
}