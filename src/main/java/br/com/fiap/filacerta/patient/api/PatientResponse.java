package br.com.fiap.filacerta.patient.api;

import br.com.fiap.filacerta.patient.domain.Patient;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String susIdentifier,
        String name,
        OffsetDateTime createdAt
) {

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getSusIdentifier(),
                patient.getName(),
                patient.getCreatedAt()
        );
    }
}