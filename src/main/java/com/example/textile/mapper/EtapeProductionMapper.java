package com.example.textile.mapper;

import com.example.textile.dto.response.EtapeProductionResponse;
import com.example.textile.entity.EtapeProduction;
import org.springframework.stereotype.Component;

@Component
public class EtapeProductionMapper {

    public EtapeProductionResponse toResponse(EtapeProduction etape) {
        return new EtapeProductionResponse(
                etape.getId(),
                etape.getTypeEtape(),
                etape.getStatut(),
                etape.getDateDebut(),
                etape.getDateFinReelle()
        );
    }
}