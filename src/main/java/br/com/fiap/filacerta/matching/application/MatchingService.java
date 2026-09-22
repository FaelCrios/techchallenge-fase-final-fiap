package br.com.fiap.filacerta.matching.application;

import br.com.fiap.filacerta.matching.api.MatchingCandidateResponse;
import br.com.fiap.filacerta.offer.domain.SlotOfferStatus;
import br.com.fiap.filacerta.offer.infrastructure.SlotOfferRepository;
import br.com.fiap.filacerta.scheduling.domain.AppointmentSlot;
import br.com.fiap.filacerta.scheduling.domain.AppointmentSlotStatus;
import br.com.fiap.filacerta.scheduling.infrastructure.AppointmentSlotRepository;
import br.com.fiap.filacerta.shared.exception.BusinessException;
import br.com.fiap.filacerta.shared.exception.NotFoundException;
import br.com.fiap.filacerta.waitlist.domain.ClinicalPriority;
import br.com.fiap.filacerta.waitlist.domain.PreferredPeriod;
import br.com.fiap.filacerta.waitlist.domain.WaitlistEntry;
import br.com.fiap.filacerta.waitlist.domain.WaitlistStatus;
import br.com.fiap.filacerta.waitlist.infrastructure.WaitlistEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class MatchingService {

    private final AppointmentSlotRepository appointmentSlotRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final SlotOfferRepository slotOfferRepository;

    private final Clock clock;

    public MatchingService(
            AppointmentSlotRepository appointmentSlotRepository,
            WaitlistEntryRepository waitlistEntryRepository,
            SlotOfferRepository slotOfferRepository,
            Clock clock
    ) {
        this.appointmentSlotRepository = appointmentSlotRepository;
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.slotOfferRepository = slotOfferRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<MatchingCandidateResponse> rankCandidates(UUID slotId){
        AppointmentSlot slot = findSlot(slotId);
        validateSlotIsAvailable(slot);
        return rankCandidatesForSlot(slot);
    }

    public List<MatchingCandidateResponse> rankCandidatesForSlot(AppointmentSlot slot) {
        PreferredPeriod slotPeriod = resolveSlotPeriod(slot.getScheduledAt());

        List<WaitlistEntry> entries = waitlistEntryRepository
                        .findByHealthUnitIdAndSpecialtyIdAndStatus(
                                slot.getHealthUnit().getId(),
                                slot.getSpecialty().getId(),
                                WaitlistStatus.WAITING
                        );

        return entries.stream()
                .filter(entry -> !hasPreviousUnsuccessfulOffer(
                        slot.getId(),
                        entry.getId()
                ))
                .map(entry -> new RankedCandidate(
                        entry,
                        calculateWaitingDays(entry.getEnteredAt()),
                        isPeriodCompatible(
                                entry.getPreferredPeriod(),
                                slotPeriod
                        )
                ))
                .sorted(candidateComparator())
                .map(this::toResponse)
                .toList();
    }

    private AppointmentSlot findSlot(UUID slotId) {
        return appointmentSlotRepository
                .findById(slotId)
                .orElseThrow(() -> new NotFoundException(
                        "Vaga não encontrada: " + slotId
                ));
    }

    private void validateSlotIsAvailable(AppointmentSlot slot) {
        if (slot.getStatus() != AppointmentSlotStatus.AVAILABLE) {
            throw new BusinessException(
                    "A vaga não está disponível para alocação"
            );
        }
    }

    private PreferredPeriod resolveSlotPeriod(OffsetDateTime scheduledAt) {
        int hour = scheduledAt.getHour();

        if (hour < 12) {
            return PreferredPeriod.MORNING;
        }

        if (hour < 18) {
            return PreferredPeriod.AFTERNOON;
        }

        return PreferredPeriod.EVENING;
    }

    private boolean isPeriodCompatible(PreferredPeriod patientPreference, PreferredPeriod slotPeriod) {
        return patientPreference == PreferredPeriod.ANY
                || patientPreference == slotPeriod;
    }

    private long calculateWaitingDays(OffsetDateTime enteredAt) {
        return Duration.between(
                enteredAt,
                OffsetDateTime.now(clock)
        ).toDays();
    }

    private int priorityWeight(ClinicalPriority priority) {
        return switch (priority) {
            case VERY_HIGH -> 4;
            case HIGH -> 3;
            case NORMAL -> 2;
            case LOW -> 1;
        };
    }

    private Comparator<RankedCandidate> candidateComparator() {
        return Comparator
                .comparingInt(
                        (RankedCandidate candidate) ->
                                priorityWeight(
                                        candidate.entry()
                                                .getClinicalPriority()
                                )
                )
                .reversed()
                .thenComparing(
                        RankedCandidate::waitingDays,
                        Comparator.reverseOrder()
                )
                .thenComparing(
                        RankedCandidate::periodCompatible,
                        Comparator.reverseOrder()
                )
                .thenComparing(
                        candidate ->
                                candidate.entry().getEnteredAt()
                );
    }

    private boolean hasPreviousUnsuccessfulOffer(UUID slotId, UUID waitlistEntryId) {
        return slotOfferRepository.existsByAppointmentSlotIdAndWaitlistEntryIdAndStatusIn(
                        slotId,
                        waitlistEntryId,
                        List.of(
                                SlotOfferStatus.REJECTED,
                                SlotOfferStatus.EXPIRED
                        )
                );
    }

    private MatchingCandidateResponse toResponse(RankedCandidate candidate) {
        WaitlistEntry entry = candidate.entry();

        return new MatchingCandidateResponse(
                entry.getId(),
                entry.getPatient().getId(),
                entry.getPatient().getName(),
                entry.getClinicalPriority(),
                entry.getPreferredPeriod(),
                candidate.waitingDays(),
                candidate.periodCompatible(),
                entry.getEnteredAt()
        );
    }

    private record RankedCandidate(
            WaitlistEntry entry,
            long waitingDays,
            boolean periodCompatible
    ) {
    }
}