package br.com.fiap.filacerta.waitlist.api;

import br.com.fiap.filacerta.waitlist.domain.ClinicalPriority;
import br.com.fiap.filacerta.waitlist.domain.PreferredPeriod;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateWaitlistEntryRequest(

        @NotNull(message = "O paciente é obrigatório")
        UUID patientId,

        @NotNull(message = "A unidade de saúde é obrigatória")
        UUID healthUnitId,

        @NotNull(message = "A especialidade é obrigatória")
        UUID specialtyId,

        @NotNull(message = "A prioridade clínica é obrigatória")
        ClinicalPriority clinicalPriority,

        @NotNull(message = "O período preferencial é obrigatório")
        PreferredPeriod preferredPeriod

) {
}