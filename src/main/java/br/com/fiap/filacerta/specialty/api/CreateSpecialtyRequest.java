package br.com.fiap.filacerta.specialty.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSpecialtyRequest(
        @NotBlank(message = "O código da especialidade  é obrigatório")
        @Size(max = 50, message="O código deve possuir no máximo 50 caracteres")
        String code,

        @NotBlank(message = "O nome da especialidade  é obrigatório")
        @Size(max = 120, message="O nome deve possuir no máximo 120 caracteres")
        String name
) {
}
