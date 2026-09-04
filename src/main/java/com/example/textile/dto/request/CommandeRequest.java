package com.example.textile.dto.request;

import com.example.textile.entity.Priorite;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CommandeRequest {

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;

    private LocalDate datePrevueLivraison;

    private Priorite priorite;

    @NotEmpty(message = "La commande doit contenir au moins une ligne")
    private List<@Valid LigneCommandeRequest> lignes;
}