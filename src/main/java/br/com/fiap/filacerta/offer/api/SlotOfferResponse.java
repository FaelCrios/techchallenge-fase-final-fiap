package br.com.fiap.filacerta.offer.api;

import br.com.fiap.filacerta.offer.domain.SlotOffer;
import br.com.fiap.filacerta.offer.domain.SlotOfferStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SlotOfferResponse(

        UUID token,

        SlotOfferStatus status,

        UUID slotId,

        UUID waitlistEntryId,

        UUID patientId,

        String patientName,

        OffsetDateTime scheduledAt,

        OffsetDateTime offeredAt,

        OffsetDateTime expiresAt,

        OffsetDateTime respondedAt

) {

    public static SlotOfferResponse from(
            SlotOffer offer
    ) {
        return new SlotOfferResponse(
                offer.getToken(),
                offer.getStatus(),

                offer.getAppointmentSlot().getId(),

                offer.getWaitlistEntry().getId(),

                offer.getWaitlistEntry()
                        .getPatient()
                        .getId(),

                offer.getWaitlistEntry()
                        .getPatient()
                        .getName(),

                offer.getAppointmentSlot()
                        .getScheduledAt(),

                offer.getOfferedAt(),
                offer.getExpiresAt(),
                offer.getRespondedAt()
        );
    }
}