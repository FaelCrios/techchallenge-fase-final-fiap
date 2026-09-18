package br.com.fiap.filacerta.healthunit.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateHealthUnitRequest(

        @NotBlank(message = "O código da unidade é obrigatório")
        @Size(
                max = 30,
                message = "O código deve possuir no máximo 30 caracteres"
        )
        String code,

        @NotBlank(message = "O nome da unidade é obrigatório")
        @Size(
                max = 150,
                message = "O nome deve possuir no máximo 150 caracteres"
        )
        String name
) {
}