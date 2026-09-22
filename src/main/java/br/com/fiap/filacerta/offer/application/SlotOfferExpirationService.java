package br.com.fiap.filacerta.offer.application;

import br.com.fiap.filacerta.offer.domain.SlotOffer;
import br.com.fiap.filacerta.offer.domain.SlotOfferStatus;
import br.com.fiap.filacerta.offer.infrastructure.SlotOfferRepository;
import br.com.fiap.filacerta.scheduling.domain.AppointmentSlot;
import br.com.fiap.filacerta.scheduling.infrastructure.AppointmentSlotRepository;
import br.com.fiap.filacerta.waitlist.domain.WaitlistEntry;
import br.com.fiap.filacerta.waitlist.infrastructure.WaitlistEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SlotOfferExpirationService {

    private final SlotOfferRepository slotOfferRepository;

    private final AppointmentSlotRepository appointmentSlotRepository;

    private final WaitlistEntryRepository waitlistEntryRepository;

    private final Clock clock;

    public SlotOfferExpirationService(
            SlotOfferRepository slotOfferRepository,
            AppointmentSlotRepository appointmentSlotRepository,
            WaitlistEntryRepository waitlistEntryRepository,
            Clock clock
    ) {
        this.slotOfferRepository = slotOfferRepository;
        this.appointmentSlotRepository = appointmentSlotRepository;
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.clock = clock;
    }

    @Transactional
    public Optional<UUID> expireOffer(UUID offerId) {
        Optional<UUID> slotIdOptional = slotOfferRepository.findSlotIdByOfferId(offerId);

        if (slotIdOptional.isEmpty()) {
            return Optional.empty();
        }

        UUID slotId = slotIdOptional.get();
        AppointmentSlot slot =
                appointmentSlotRepository
                        .findByIdForUpdate(slotId)
                        .orElseThrow(() ->
                                new IllegalStateException("Vaga não encontrada: " + slotId));

        SlotOffer offer =
                slotOfferRepository
                        .findByIdForUpdate(offerId)
                        .orElseThrow(() ->
                                new IllegalStateException("Oferta não encontrada: " + offerId)
                        );

        if (!offer.getAppointmentSlot().getId().equals(slotId)) {
            throw new IllegalStateException("A oferta não pertence à vaga informada");
        }

        if (offer.getStatus() != SlotOfferStatus.PENDING) {
            return Optional.empty();
        }

        OffsetDateTime now = OffsetDateTime.now(clock);

        if (now.isBefore(offer.getExpiresAt())) {
            return Optional.empty();
        }

        UUID entryId = offer.getWaitlistEntry().getId();

        WaitlistEntry entry =
                waitlistEntryRepository
                        .findByIdForUpdate(entryId)
                        .orElseThrow(() ->
                                new IllegalStateException("Entrada da fila não encontrada: " + entryId)
                        );

        offer.expire(clock);
        slot.release();
        entry.returnToWaiting();
        return Optional.of(slotId);
    }
}