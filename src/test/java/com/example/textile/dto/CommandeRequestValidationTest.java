package com.example.textile.dto;

import com.example.textile.dto.request.CommandeRequest;
import com.example.textile.dto.request.LigneCommandeRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CommandeRequestValidationTest {

    private final Validator validator;

    CommandeRequestValidationTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void requeteInvalide_sansClientNiLignes_doitEchouer() {
        CommandeRequest request = new CommandeRequest(); // tout est null

        Set<ConstraintViolation<CommandeRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty(); // on s'attend à des erreurs
    }

    @Test
    void requeteValide_doitPasser() {
        CommandeRequest request = new CommandeRequest();
        request.setClientId(1L);

        LigneCommandeRequest ligne = new LigneCommandeRequest();
        ligne.setProduitId(1L);
        ligne.setQuantite(10);
        request.setLignes(List.of(ligne));

        Set<ConstraintViolation<CommandeRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty(); // aucune erreur attendue
    }
}