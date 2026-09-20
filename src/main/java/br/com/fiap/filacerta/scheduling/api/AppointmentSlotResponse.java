package br.com.fiap.filacerta.scheduling.api;

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

        OffsetDateTime scheduledAt,
        AppointmentSlotStatus status,
        OffsetDateTime createdAt
) {
    public static AppointmentSlotResponse from(AppointmentSlot appointmentSlot) {
        return new AppointmentSlotResponse(
                appointmentSlot.getId(),

                appointmentSlot.getHealthUnit().getId(),
                appointmentSlot.getHealthUnit().getName(),

                appointmentSlot.getSpecialty().getId(),
                appointmentSlot.getSpecialty().getName(),

                appointmentSlot.getScheduledAt(),
                appointmentSlot.getStatus(),
                appointmentSlot.getCreatedAt()
        );
    }
}
