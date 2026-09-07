package com.example.textile.service;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.response.CommandeResponse;

import java.util.List;

public interface CommandeService {
    CommandeResponse creerCommande(CommandeRequest request);
    List<CommandeResponse> listerCommandes();
    CommandeResponse obtenirCommande(Long id);
}
