package com.example.textile.service;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.response.CommandeResponse;

public interface CommandeService {
    CommandeResponse creerCommande(CommandeRequest request);
}