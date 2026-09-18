package br.com.fiap.filacerta.patient.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePatientRequest(
        @NotBlank(message = "O identificador SUS é obrigatório")
        @Size(
                max = 30,
                message = "O identificador SUS deve possuir no máximo 30 caracteres"
        )
        String susIdentifier,

        @NotBlank(message = "O nome do paciente é obrigatório")
        @Size(
                max = 150,
                message = "O nome deve possuir no máximo 150 caracteres"
        )
        String name
) {
}
