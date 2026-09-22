package br.com.fiap.filacerta.professional.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateHealthProfessionalRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150)
        String name,

        @NotBlank(
                message = "O registro profissional é obrigatório"
        )
        @Size(max = 50)
        String professionalRegistration,

        @NotNull(
                message = "A unidade de saúde é obrigatória"
        )
        UUID healthUnitId,

        @NotNull(
                message = "A especialidade é obrigatória"
        )
        UUID specialtyId

) {
}