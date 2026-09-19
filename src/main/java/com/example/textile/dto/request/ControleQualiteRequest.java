package com.example.textile.dto.request;

import com.example.textile.entity.ResultatQualite;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ControleQualiteRequest {

    @NotNull(message = "Le résultat du contrôle qualité est obligatoire")
    private ResultatQualite resultat;

    private String commentaire;
}