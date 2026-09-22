package br.com.fiap.filacerta.scheduling.api;

import br.com.fiap.filacerta.professional.domain.HealthProfessional;
import br.com.fiap.filacerta.scheduling.domain.AppointmentSlot;
import br.com.fiap.filacerta.scheduling.domain.AppointmentSlotStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentSlotResponse(
        UUID id,

        UUID healthUnitId,
        String healthUnitName,

        UUID specialtyId,
        String specialtyName,

        UUID professionalId,
        String professionalName,
        OffsetDateTime scheduledAt,
        AppointmentSlotStatus status,
        OffsetDateTime createdAt
) {

    public static AppointmentSlotResponse from(AppointmentSlot appointmentSlot) {
        HealthProfessional professional =
                appointmentSlot.getProfessional();

        return new AppointmentSlotResponse(
                appointmentSlot.getId(),

                appointmentSlot.getHealthUnit().getId(),
                appointmentSlot.getHealthUnit().getName(),

                appointmentSlot.getSpecialty().getId(),
                appointmentSlot.getSpecialty().getName(),

                professional != null ? professional.getId() : null,
                professional != null ? professional.getName() : null,

                appointmentSlot.getScheduledAt(),
                appointmentSlot.getStatus(),
                appointmentSlot.getCreatedAt()
        );
    }
}