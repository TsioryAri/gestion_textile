package com.example.textile.controller.rest;

import com.example.textile.dto.response.EtapeProductionResponse;
import com.example.textile.entity.EtapeProduction;
import com.example.textile.entity.TypeEtape;
import com.example.textile.mapper.EtapeProductionMapper;
import com.example.textile.repository.EtapeProductionRepository;
import com.example.textile.service.ProductionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders/{commandeId}/production")
public class ProductionRestController {

    private final ProductionService productionService;
    private final EtapeProductionRepository etapeProductionRepository;
    private final EtapeProductionMapper etapeProductionMapper;

    public ProductionRestController(ProductionService productionService,
                                    EtapeProductionRepository etapeProductionRepository,
                                    EtapeProductionMapper etapeProductionMapper) {
        this.productionService = productionService;
        this.etapeProductionRepository = etapeProductionRepository;
        this.etapeProductionMapper = etapeProductionMapper;
    }

    @GetMapping
    public ResponseEntity<List<EtapeProductionResponse>> listerEtapes(@PathVariable Long commandeId) {
        List<EtapeProductionResponse> etapes = etapeProductionRepository.findByCommandeId(commandeId).stream()
                .map(etapeProductionMapper::toResponse)
                .toList();
        return ResponseEntity.ok(etapes);
    }

    @PostMapping("/steps/{step}/start")
    public ResponseEntity<EtapeProductionResponse> demarrerEtape(@PathVariable Long commandeId,
                                                                 @PathVariable TypeEtape step) {
        EtapeProduction etape = productionService.demarrerEtape(commandeId, step);
        return ResponseEntity.ok(etapeProductionMapper.toResponse(etape));
    }

    @PostMapping("/steps/{step}/complete")
    public ResponseEntity<EtapeProductionResponse> terminerEtape(@PathVariable Long commandeId,
                                                                 @PathVariable TypeEtape step) {
        EtapeProduction etape = productionService.terminerEtape(commandeId, step);
        return ResponseEntity.ok(etapeProductionMapper.toResponse(etape));
    }
}