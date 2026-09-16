package com.example.textile.dto.response;

import com.example.textile.entity.StatutEtape;
import com.example.textile.entity.TypeEtape;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EtapeProductionResponse {
    private Long id;
    private TypeEtape typeEtape;
    private StatutEtape statut;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFinReelle;
}