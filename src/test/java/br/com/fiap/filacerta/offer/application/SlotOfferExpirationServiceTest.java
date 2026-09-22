package br.com.fiap.filacerta.offer.application;

import br.com.fiap.filacerta.offer.domain.SlotOffer;
import br.com.fiap.filacerta.offer.domain.SlotOfferStatus;
import br.com.fiap.filacerta.offer.infrastructure.SlotOfferRepository;

import br.com.fiap.filacerta.scheduling.domain.AppointmentSlot;
import br.com.fiap.filacerta.scheduling.infrastructure.AppointmentSlotRepository;

import br.com.fiap.filacerta.waitlist.domain.WaitlistEntry;
import br.com.fiap.filacerta.waitlist.infrastructure.WaitlistEntryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SlotOfferExpirationServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-10-10T10:20:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private SlotOfferRepository slotOfferRepository;

    @Mock
    private AppointmentSlotRepository appointmentSlotRepository;

    @Mock
    private WaitlistEntryRepository waitlistEntryRepository;

    private SlotOfferExpirationService expirationService;

    private AutoCloseable mocks;

    private UUID offerId;
    private UUID slotId;
    private UUID entryId;

    private AppointmentSlot slot;
    private WaitlistEntry entry;
    private SlotOffer offer;


    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        expirationService = new SlotOfferExpirationService(
                slotOfferRepository,
                appointmentSlotRepository,
                waitlistEntryRepository,
                FIXED_CLOCK
        );

        offerId = UUID.randomUUID();
        slotId = UUID.randomUUID();
        entryId = UUID.randomUUID();

        slot = mock(AppointmentSlot.class);
        entry = mock(WaitlistEntry.class);
        offer = mock(SlotOffer.class);
    }


    @Test
    void shouldExpireOfferAndReleaseSlot() {
        configureExistingOffer();

        when(offer.getStatus()).thenReturn(SlotOfferStatus.PENDING);

        when(offer.getExpiresAt()).thenReturn(OffsetDateTime.parse("2026-10-10T10:15:00Z"));

        when(offer.getWaitlistEntry()).thenReturn(entry);

        when(entry.getId()).thenReturn(entryId);

        when(waitlistEntryRepository.findByIdForUpdate(entryId)).thenReturn(Optional.of(entry));

        Optional<UUID> result = expirationService.expireOffer(offerId);

        assertEquals(Optional.of(slotId), result);

        verify(offer).expire(FIXED_CLOCK);

        verify(slot).release();

        verify(entry).returnToWaiting();
    }

    @Test
    void shouldNotExpireAcceptedOffer() {
        configureExistingOffer();

        when(offer.getStatus()).thenReturn(SlotOfferStatus.ACCEPTED);

        Optional<UUID> result = expirationService.expireOffer(offerId);

        assertTrue(result.isEmpty());

        verify(offer, never()).expire(any(Clock.class));

        verify(slot, never()).release();

        verifyNoInteractions(waitlistEntryRepository);
    }

    @Test
    void shouldNotExpireRejectedOffer() {
        configureExistingOffer();

        when(offer.getStatus()).thenReturn(SlotOfferStatus.REJECTED);

        Optional<UUID> result = expirationService.expireOffer(offerId);

        assertTrue(result.isEmpty());

        verify(offer, never()).expire(any(Clock.class));

        verify(slot, never()).release();

        verifyNoInteractions(waitlistEntryRepository);
    }

    @Test
    void shouldNotExpireOfferBeforeDeadline() {
        configureExistingOffer();

        when(offer.getStatus()).thenReturn(SlotOfferStatus.PENDING);

        when(offer.getExpiresAt()).thenReturn(OffsetDateTime.parse("2026-10-10T10:25:00Z"));

        Optional<UUID> result = expirationService.expireOffer(offerId);

        assertTrue(result.isEmpty());

        verify(offer, never()).expire(any(Clock.class));

        verify(slot, never()).release();

        verifyNoInteractions(waitlistEntryRepository);
    }

    @Test
    void shouldReturnEmptyWhenOfferDoesNotExist() {
        when(slotOfferRepository.findSlotIdByOfferId(offerId)).thenReturn(Optional.empty());

        Optional<UUID> result = expirationService.expireOffer(offerId);

        assertTrue(result.isEmpty());

        verifyNoInteractions(appointmentSlotRepository);

        verifyNoInteractions(waitlistEntryRepository);

        verify(slotOfferRepository, never()).findByIdForUpdate(any(UUID.class));
    }


    @Test
    void shouldThrowExceptionWhenSlotDoesNotExist() {
        when(slotOfferRepository.findSlotIdByOfferId(offerId)).thenReturn(Optional.of(slotId));

        when(appointmentSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> expirationService.expireOffer(offerId));

        assertEquals("Vaga não encontrada: " + slotId, exception.getMessage());

        verify(slotOfferRepository, never()).findByIdForUpdate(any(UUID.class));

        verifyNoInteractions(waitlistEntryRepository);
    }

    private void configureExistingOffer() {
        when(slotOfferRepository.findSlotIdByOfferId(offerId)).thenReturn(Optional.of(slotId));

        when(appointmentSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.of(slot));

        when(slotOfferRepository.findByIdForUpdate(offerId)).thenReturn(Optional.of(offer));

        when(offer.getAppointmentSlot()).thenReturn(slot);

        when(slot.getId()).thenReturn(slotId);
    }
}