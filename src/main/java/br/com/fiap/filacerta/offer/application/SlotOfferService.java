package br.com.fiap.filacerta.offer.application;

import br.com.fiap.filacerta.matching.api.MatchingCandidateResponse;
import br.com.fiap.filacerta.matching.application.MatchingService;
import br.com.fiap.filacerta.offer.api.SlotOfferResponse;
import br.com.fiap.filacerta.offer.domain.SlotOffer;
import br.com.fiap.filacerta.offer.domain.SlotOfferStatus;
import br.com.fiap.filacerta.offer.infrastructure.SlotOfferRepository;
import br.com.fiap.filacerta.scheduling.domain.AppointmentSlot;
import br.com.fiap.filacerta.scheduling.domain.AppointmentSlotStatus;
import br.com.fiap.filacerta.scheduling.infrastructure.AppointmentSlotRepository;
import br.com.fiap.filacerta.shared.exception.BusinessException;
import br.com.fiap.filacerta.shared.exception.NotFoundException;
import br.com.fiap.filacerta.waitlist.domain.WaitlistEntry;
import br.com.fiap.filacerta.waitlist.domain.WaitlistStatus;
import br.com.fiap.filacerta.waitlist.infrastructure.WaitlistEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SlotOfferService {

    private final SlotOfferRepository slotOfferRepository;

    private final AppointmentSlotRepository appointmentSlotRepository;

    private final WaitlistEntryRepository waitlistEntryRepository;

    private final MatchingService matchingService;

    private final Clock clock;

    public SlotOfferService(SlotOfferRepository slotOfferRepository, AppointmentSlotRepository appointmentSlotRepository, WaitlistEntryRepository waitlistEntryRepository, MatchingService matchingService, Clock clock) {
        this.slotOfferRepository = slotOfferRepository;
        this.appointmentSlotRepository = appointmentSlotRepository;
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.matchingService = matchingService;
        this.clock = clock;
    }

    @Transactional
    public SlotOfferResponse createOffer(UUID slotId){
        AppointmentSlot slot = findSlotForUpdate(slotId);
        validateSlotIsAvailable(slot);

        List<MatchingCandidateResponse> candidates = matchingService.rankCandidatesForSlot(slot);
        WaitlistEntry entry = findFirstAvailableCandidate(candidates);
        slot.markAsOffered();
        entry.markAsOffered();

        SlotOffer offer = new SlotOffer(slot, entry, clock);

        SlotOffer savedOffer = slotOfferRepository.save(offer);

        return SlotOfferResponse.from(savedOffer);
    }

    private WaitlistEntry findFirstAvailableCandidate(List<MatchingCandidateResponse> candidates) {
        if (candidates.isEmpty()) {
            throw new BusinessException("Não existem pacientes elegíveis para esta vaga");
        }
        for (MatchingCandidateResponse candidate : candidates) {
            WaitlistEntry entry =
                    waitlistEntryRepository
                            .findByIdForUpdate(candidate.waitlistEntryId())
                            .orElseThrow(() -> new NotFoundException("Entrada da fila não encontrada: "+ candidate.waitlistEntryId()));
            if (entry.getStatus() == WaitlistStatus.WAITING) {
                return entry;
            }
        }
        throw new BusinessException("Nenhum candidato permanece disponível para esta vaga");
    }

    private AppointmentSlot findSlotForUpdate(UUID slotId) {

        return appointmentSlotRepository
                .findByIdForUpdate(slotId)
                .orElseThrow(() -> new NotFoundException("Vaga não encontrada: " + slotId));
    }

    private void validateSlotIsAvailable(AppointmentSlot slot) {
        if (slot.getStatus() != AppointmentSlotStatus.AVAILABLE) {
            throw new BusinessException("A vaga não está disponível para oferta");
        }
        if (!slot.getScheduledAt()
                .isAfter(OffsetDateTime.now(clock))) {
            throw new BusinessException("Não é possível oferecer uma vaga com horário expirado");
        }
    }

    @Transactional(readOnly = true)
    public SlotOfferResponse findByToken(UUID token){
        SlotOffer offer = slotOfferRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Oferta não encontrada"));
        return SlotOfferResponse.from(offer);
    }

    @Transactional
    public SlotOfferResponse acceptOffer(UUID token){
        SlotOffer offer = findOfferForUpdate(token);
        AppointmentSlot slot = offer.getAppointmentSlot();
        WaitlistEntry entry = offer.getWaitlistEntry();
        validateOfferIsPending(offer);
        offer.accept(clock);
        slot.markAsBooked();
        entry.markAsScheduled();
        return SlotOfferResponse.from(offer);
    }

    @Transactional
    public SlotOfferResponse rejectOffer(UUID token) {
        SlotOffer offer = findOfferForUpdate(token);
        AppointmentSlot slot = offer.getAppointmentSlot();
        WaitlistEntry entry = offer.getWaitlistEntry();
        validateOfferIsPending(offer);
        offer.reject(clock);
        slot.release();
        entry.returnToWaiting();
        return SlotOfferResponse.from(offer);
    }

    private SlotOffer findOfferForUpdate(UUID token) {
        return slotOfferRepository
                .findByTokenForUpdate(token)
                .orElseThrow(() -> new NotFoundException("Oferta não encontrada"));
    }

    private void validateOfferIsPending(SlotOffer offer) {
        if (offer.getStatus() != SlotOfferStatus.PENDING) {
            throw new BusinessException("A oferta não está pendente");
        }
    }

}
