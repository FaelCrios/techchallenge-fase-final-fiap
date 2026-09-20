package br.com.fiap.filacerta.scheduling.api;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateAppointmentSlotRequest(

        @NotNull(message = "A unidade de saúde é obrigatória")
        UUID healthUnitId,

        @NotNull(message = "A especialidade é obrigatória")
        UUID specialtyId,

        @NotNull(message = "A data e horário da vaga são obrigatórios")
        @Future(message = "A vaga deve possuir uma data futura")
        OffsetDateTime scheduledAt

) {
}