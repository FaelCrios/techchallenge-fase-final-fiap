package br.com.fiap.filacerta.offer.application;

import br.com.fiap.filacerta.offer.domain.SlotOfferStatus;
import br.com.fiap.filacerta.offer.infrastructure.SlotOfferRepository;
import br.com.fiap.filacerta.shared.exception.BusinessException;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotOfferExpirationSchedulerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-10-10T10:20:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private SlotOfferRepository slotOfferRepository;

    @Mock
    private SlotOfferExpirationService expirationService;

    @Mock
    private SlotOfferService slotOfferService;

    private SlotOfferExpirationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SlotOfferExpirationScheduler(
                slotOfferRepository,
                expirationService,
                slotOfferService,
                FIXED_CLOCK
        );
    }


    @Test
    void shouldProcessAllExpiredOffers() {
        UUID firstOfferId = UUID.randomUUID();
        UUID secondOfferId = UUID.randomUUID();

        UUID firstSlotId = UUID.randomUUID();
        UUID secondSlotId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(FIXED_CLOCK);

        when(slotOfferRepository.findExpiredOfferIds(
                SlotOfferStatus.PENDING,
                now
        )).thenReturn(
                List.of(firstOfferId, secondOfferId)
        );

        when(expirationService.expireOffer(firstOfferId))
                .thenReturn(Optional.of(firstSlotId));

        when(expirationService.expireOffer(secondOfferId))
                .thenReturn(Optional.of(secondSlotId));

        scheduler.processExpiredOffers();

        verify(expirationService).expireOffer(firstOfferId);

        verify(expirationService).expireOffer(secondOfferId);

        verify(slotOfferService).createOffer(firstSlotId);

        verify(slotOfferService).createOffer(secondSlotId);
    }


    @Test
    void shouldDoNothingWhenThereAreNoExpiredOffers() {
        OffsetDateTime now = OffsetDateTime.now(FIXED_CLOCK);

        when(slotOfferRepository.findExpiredOfferIds(
                SlotOfferStatus.PENDING,
                now
        )).thenReturn(List.of());

        scheduler.processExpiredOffers();

        verify(slotOfferRepository).findExpiredOfferIds(
                SlotOfferStatus.PENDING,
                now
        );
        verifyNoInteractions(expirationService);
        verifyNoInteractions(slotOfferService);
    }


    @Test
    void shouldNotCreateNewOfferWhenExpirationReturnsEmpty() {
        UUID offerId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(FIXED_CLOCK);

        when(slotOfferRepository.findExpiredOfferIds(
                SlotOfferStatus.PENDING,
                now
        )).thenReturn(List.of(offerId));

        when(expirationService.expireOffer(offerId))
                .thenReturn(Optional.empty());

        scheduler.processExpiredOffers();
        verify(expirationService).expireOffer(offerId);
        verifyNoInteractions(slotOfferService);
    }


    @Test
    void shouldKeepSlotAvailableWhenNoCandidatesExist() {
        UUID offerId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(FIXED_CLOCK);

        when(slotOfferRepository.findExpiredOfferIds(
                SlotOfferStatus.PENDING,
                now
        )).thenReturn(List.of(offerId));

        when(expirationService.expireOffer(offerId))
                .thenReturn(Optional.of(slotId));

        when(slotOfferService.createOffer(slotId))
                .thenThrow(new BusinessException("Não existem pacientes elegíveis para esta vaga")
                );

        assertDoesNotThrow(
                () -> scheduler.processExpiredOffers()
        );

        verify(expirationService).expireOffer(offerId);
        verify(slotOfferService).createOffer(slotId);
    }


    @Test
    void shouldContinueProcessingWhenExpirationFails() {
        UUID firstOfferId = UUID.randomUUID();
        UUID secondOfferId = UUID.randomUUID();

        UUID secondSlotId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(FIXED_CLOCK);

        when(slotOfferRepository.findExpiredOfferIds(
                SlotOfferStatus.PENDING,
                now
        )).thenReturn(
                List.of(firstOfferId, secondOfferId)
        );

        when(expirationService.expireOffer(firstOfferId)).thenThrow(new IllegalStateException("Erro ao expirar oferta"));

        when(expirationService.expireOffer(secondOfferId))
                .thenReturn(Optional.of(secondSlotId));

        assertDoesNotThrow(
                () -> scheduler.processExpiredOffers()
        );

        verify(expirationService).expireOffer(firstOfferId);

        verify(expirationService).expireOffer(secondOfferId);

        verify(slotOfferService, times(1))
                .createOffer(secondSlotId);

        verify(slotOfferService).createOffer(secondSlotId);
    }


    @Test
    void shouldContinueProcessingWhenNewOfferFails() {
        UUID firstOfferId = UUID.randomUUID();
        UUID secondOfferId = UUID.randomUUID();

        UUID firstSlotId = UUID.randomUUID();
        UUID secondSlotId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(FIXED_CLOCK);

        when(slotOfferRepository.findExpiredOfferIds(
                SlotOfferStatus.PENDING,
                now
        )).thenReturn(
                List.of(firstOfferId, secondOfferId)
        );

        when(expirationService.expireOffer(firstOfferId))
                .thenReturn(Optional.of(firstSlotId));

        when(expirationService.expireOffer(secondOfferId))
                .thenReturn(Optional.of(secondSlotId));

        when(slotOfferService.createOffer(firstSlotId))
                .thenThrow(new IllegalStateException("Erro inesperado ao criar oferta")
                );

        assertDoesNotThrow(
                () -> scheduler.processExpiredOffers()
        );

        verify(expirationService).expireOffer(firstOfferId);

        verify(expirationService).expireOffer(secondOfferId);

        verify(slotOfferService).createOffer(firstSlotId);

        verify(slotOfferService).createOffer(secondSlotId);
    }
}