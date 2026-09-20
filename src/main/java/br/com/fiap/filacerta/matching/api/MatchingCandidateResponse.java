package br.com.fiap.filacerta.matching.api;

import br.com.fiap.filacerta.waitlist.domain.ClinicalPriority;
import br.com.fiap.filacerta.waitlist.domain.PreferredPeriod;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MatchingCandidateResponse(

        UUID waitlistEntryId,

        UUID patientId,
        String patientName,

        ClinicalPriority clinicalPriority,
        PreferredPeriod preferredPeriod,

        long waitingDays,
        boolean periodCompatible,

        OffsetDateTime enteredAt

) {
}