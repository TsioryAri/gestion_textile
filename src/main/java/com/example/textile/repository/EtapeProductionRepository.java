package com.example.textile.repository;

import com.example.textile.entity.EtapeProduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EtapeProductionRepository extends JpaRepository<EtapeProduction, Long> {
    List<EtapeProduction> findByCommandeId(Long commandeId);
}