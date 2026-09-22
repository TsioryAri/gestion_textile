package com.example.textile.controller.rest;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.request.ControleQualiteRequest;
import com.example.textile.dto.response.CommandeResponse;
import com.example.textile.dto.response.EtapeProductionResponse;
import com.example.textile.entity.EtapeProduction;
import com.example.textile.mapper.EtapeProductionMapper;
import com.example.textile.service.CommandeService;
import com.example.textile.service.ProductionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class CommandeRestController {

    private final CommandeService commandeService;
    private final ProductionService productionService;
    private final EtapeProductionMapper etapeProductionMapper;

    public CommandeRestController(CommandeService commandeService,
                                  ProductionService productionService,
                                  EtapeProductionMapper etapeProductionMapper) {
        this.commandeService = commandeService;
        this.productionService = productionService;
        this.etapeProductionMapper = etapeProductionMapper;
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<CommandeResponse> creerCommande(@Valid @RequestBody CommandeRequest request) {
        CommandeResponse response = commandeService.creerCommande(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR','USER')")
    @GetMapping
    public ResponseEntity<List<CommandeResponse>> listerCommandes() {
        return ResponseEntity.ok(commandeService.listerCommandes());
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR','USER')")
    @GetMapping("/delayed")
    public ResponseEntity<List<CommandeResponse>> listerCommandesEnRetard() {
        return ResponseEntity.ok(commandeService.listerCommandesEnRetard());
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR','USER')")
    @GetMapping("/{id}")
    public ResponseEntity<CommandeResponse> obtenirCommande(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.obtenirCommande(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    @PostMapping("/{id}/quality-control")
    public ResponseEntity<EtapeProductionResponse> effectuerControleQualite(
            @PathVariable Long id,
            @Valid @RequestBody ControleQualiteRequest request) {
        EtapeProduction etape = productionService.effectuerControleQualite(
                id, request.getResultat(), request.getCommentaire());
        return ResponseEntity.ok(etapeProductionMapper.toResponse(etape));
    }
}