package br.com.fiap.filacerta.professional.api;

import br.com.fiap.filacerta.professional.domain.HealthProfessional;
import java.time.OffsetDateTime;
import java.util.UUID;

public record HealthProfessionalResponse(

        UUID id,
        String name,
        String professionalRegistration,

        UUID healthUnitId,
        String healthUnitName,

        UUID specialtyId,
        String specialtyName,

        OffsetDateTime createdAt

) {

    public static HealthProfessionalResponse from(
            HealthProfessional professional
    ) {
        return new HealthProfessionalResponse(
                professional.getId(),
                professional.getName(),
                professional.getProfessionalRegistration(),

                professional.getHealthUnit().getId(),
                professional.getHealthUnit().getName(),

                professional.getSpecialty().getId(),
                professional.getSpecialty().getName(),

                professional.getCreatedAt()
        );
    }
}