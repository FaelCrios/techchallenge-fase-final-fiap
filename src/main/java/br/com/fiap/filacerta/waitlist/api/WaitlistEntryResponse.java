package br.com.fiap.filacerta.waitlist.api;

import br.com.fiap.filacerta.waitlist.domain.ClinicalPriority;
import br.com.fiap.filacerta.waitlist.domain.PreferredPeriod;
import br.com.fiap.filacerta.waitlist.domain.WaitlistEntry;
import br.com.fiap.filacerta.waitlist.domain.WaitlistStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WaitlistEntryResponse(

        UUID id,

        UUID patientId,
        String patientName,

        UUID healthUnitId,
        String healthUnitName,

        UUID specialtyId,
        String specialtyName,

        ClinicalPriority clinicalPriority,
        PreferredPeriod preferredPeriod,
        WaitlistStatus status,

        OffsetDateTime enteredAt

) {

    public static WaitlistEntryResponse from(
            WaitlistEntry entry
    ) {
        return new WaitlistEntryResponse(
                entry.getId(),

                entry.getPatient().getId(),
                entry.getPatient().getName(),

                entry.getHealthUnit().getId(),
                entry.getHealthUnit().getName(),

                entry.getSpecialty().getId(),
                entry.getSpecialty().getName(),

                entry.getClinicalPriority(),
                entry.getPreferredPeriod(),
                entry.getStatus(),

                entry.getEnteredAt()
        );
    }
}