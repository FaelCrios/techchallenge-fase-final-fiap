package br.com.fiap.filacerta.matching.application;

import br.com.fiap.filacerta.healthunit.domain.HealthUnit;
import br.com.fiap.filacerta.offer.infrastructure.SlotOfferRepository;
import br.com.fiap.filacerta.patient.domain.Patient;
import br.com.fiap.filacerta.scheduling.domain.AppointmentSlot;
import br.com.fiap.filacerta.scheduling.domain.AppointmentSlotStatus;
import br.com.fiap.filacerta.scheduling.infrastructure.AppointmentSlotRepository;
import br.com.fiap.filacerta.shared.exception.BusinessException;
import br.com.fiap.filacerta.specialty.domain.Specialty;
import br.com.fiap.filacerta.waitlist.domain.ClinicalPriority;
import br.com.fiap.filacerta.waitlist.domain.PreferredPeriod;
import br.com.fiap.filacerta.waitlist.domain.WaitlistEntry;
import br.com.fiap.filacerta.waitlist.domain.WaitlistStatus;
import br.com.fiap.filacerta.waitlist.infrastructure.WaitlistEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    private static final Instant FIXED_INSTANT =
            Instant.parse("2026-09-18T15:00:00Z");

    private static final Clock FIXED_CLOCK =
            Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    @Mock
    private AppointmentSlotRepository appointmentSlotRepository;

    @Mock
    private WaitlistEntryRepository waitlistEntryRepository;

    @Mock
    private SlotOfferRepository slotOfferRepository;

    @Mock
    private AppointmentSlot slot;

    @Mock
    private HealthUnit healthUnit;

    @Mock
    private Specialty specialty;

    private MatchingService matchingService;

    private UUID slotId;
    private UUID healthUnitId;
    private UUID specialtyId;

    @BeforeEach
    void setUp() {
        matchingService = new MatchingService(
                appointmentSlotRepository,
                waitlistEntryRepository,
                slotOfferRepository,
                FIXED_CLOCK
        );

        slotId = UUID.randomUUID();
        healthUnitId = UUID.randomUUID();
        specialtyId = UUID.randomUUID();
    }

    @Test
    void shouldRankCandidatesByClinicalPriority() {
        prepareAvailableSlot();

        WaitlistEntry normalCandidate = createCandidate(
                "Carlos Demo",
                ClinicalPriority.NORMAL,
                PreferredPeriod.MORNING,
                30
        );

        WaitlistEntry veryHighCandidate = createCandidate(
                "Maria Demo",
                ClinicalPriority.VERY_HIGH,
                PreferredPeriod.MORNING,
                5
        );

        WaitlistEntry highCandidate = createCandidate(
                "João Demo",
                ClinicalPriority.HIGH,
                PreferredPeriod.MORNING,
                10
        );

        when(waitlistEntryRepository
                .findByHealthUnitIdAndSpecialtyIdAndStatus(
                        healthUnitId,
                        specialtyId,
                        WaitlistStatus.WAITING
                ))
                .thenReturn(List.of(
                        normalCandidate,
                        veryHighCandidate,
                        highCandidate
                ));

        var result = matchingService.rankCandidates(slotId);

        assertEquals(3, result.size());

        assertEquals(
                ClinicalPriority.VERY_HIGH,
                result.get(0).clinicalPriority()
        );

        assertEquals(
                ClinicalPriority.HIGH,
                result.get(1).clinicalPriority()
        );

        assertEquals(
                ClinicalPriority.NORMAL,
                result.get(2).clinicalPriority()
        );
    }

    @Test
    void shouldRankOlderCandidateFirstWhenPriorityIsTheSame() {
        prepareAvailableSlot();

        WaitlistEntry newerCandidate = createCandidate(
                "Paciente Novo",
                ClinicalPriority.HIGH,
                PreferredPeriod.MORNING,
                5
        );

        WaitlistEntry olderCandidate = createCandidate(
                "Paciente Antigo",
                ClinicalPriority.HIGH,
                PreferredPeriod.MORNING,
                30
        );

        when(waitlistEntryRepository
                .findByHealthUnitIdAndSpecialtyIdAndStatus(
                        healthUnitId,
                        specialtyId,
                        WaitlistStatus.WAITING
                ))
                .thenReturn(List.of(
                        newerCandidate,
                        olderCandidate
                ));

        var result = matchingService.rankCandidates(slotId);

        assertEquals(2, result.size());

        assertEquals(
                "Paciente Antigo",
                result.get(0).patientName()
        );

        assertEquals(30, result.get(0).waitingDays());

        assertEquals(
                "Paciente Novo",
                result.get(1).patientName()
        );

        assertEquals(5, result.get(1).waitingDays());
    }

    @Test
    void shouldSearchOnlyWaitingCandidatesFromSlotUnitAndSpecialty() {
        prepareAvailableSlot();

        when(waitlistEntryRepository
                .findByHealthUnitIdAndSpecialtyIdAndStatus(
                        healthUnitId,
                        specialtyId,
                        WaitlistStatus.WAITING
                ))
                .thenReturn(List.of());

        var result = matchingService.rankCandidates(slotId);

        assertEquals(0, result.size());

        verify(waitlistEntryRepository)
                .findByHealthUnitIdAndSpecialtyIdAndStatus(
                        healthUnitId,
                        specialtyId,
                        WaitlistStatus.WAITING
                );
    }

    @Test
    void shouldThrowExceptionWhenSlotIsNotAvailable() {
        when(appointmentSlotRepository.findById(slotId))
                .thenReturn(Optional.of(slot));

        when(slot.getStatus())
                .thenReturn(AppointmentSlotStatus.BOOKED);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchingService.rankCandidates(slotId)
        );

        assertEquals(
                "A vaga não está disponível para alocação",
                exception.getMessage()
        );
    }

    private void prepareAvailableSlot() {
        when(appointmentSlotRepository.findById(slotId))
                .thenReturn(Optional.of(slot));

        when(slot.getStatus())
                .thenReturn(AppointmentSlotStatus.AVAILABLE);

        when(slot.getScheduledAt())
                .thenReturn(
                        OffsetDateTime.of(
                                2026,
                                10,
                                10,
                                9,
                                0,
                                0,
                                0,
                                ZoneOffset.ofHours(-3)
                        )
                );

        when(slot.getHealthUnit())
                .thenReturn(healthUnit);

        when(slot.getSpecialty())
                .thenReturn(specialty);

        when(healthUnit.getId())
                .thenReturn(healthUnitId);

        when(specialty.getId())
                .thenReturn(specialtyId);
    }

    private WaitlistEntry createCandidate(
            String patientName,
            ClinicalPriority clinicalPriority,
            PreferredPeriod preferredPeriod,
            long waitingDays
    ) {
        WaitlistEntry entry = org.mockito.Mockito.mock(
                WaitlistEntry.class
        );

        Patient patient = org.mockito.Mockito.mock(
                Patient.class
        );

        UUID waitlistEntryId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        OffsetDateTime enteredAt =
                OffsetDateTime.now(FIXED_CLOCK)
                        .minusDays(waitingDays);

        when(entry.getId())
                .thenReturn(waitlistEntryId);

        when(entry.getPatient())
                .thenReturn(patient);

        when(entry.getClinicalPriority())
                .thenReturn(clinicalPriority);

        when(entry.getPreferredPeriod())
                .thenReturn(preferredPeriod);

        when(entry.getEnteredAt())
                .thenReturn(enteredAt);

        when(patient.getId())
                .thenReturn(patientId);

        when(patient.getName())
                .thenReturn(patientName);

        return entry;
    }
}