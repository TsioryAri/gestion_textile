package com.example.textile.controller.rest;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.response.CommandeResponse;
import com.example.textile.service.CommandeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class CommandeRestController {

    private final CommandeService commandeService;

    public CommandeRestController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    @PostMapping
    public ResponseEntity<CommandeResponse> creerCommande(@Valid @RequestBody CommandeRequest request) {
        CommandeResponse response = commandeService.creerCommande(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CommandeResponse>> listerCommandes() {
        return ResponseEntity.ok(commandeService.listerCommandes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeResponse> obtenirCommande(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.obtenirCommande(id));
    }
}