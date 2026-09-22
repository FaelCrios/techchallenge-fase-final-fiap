package br.com.fiap.filacerta.offer.domain;

import br.com.fiap.filacerta.scheduling.domain.AppointmentSlot;
import br.com.fiap.filacerta.shared.exception.BusinessException;
import br.com.fiap.filacerta.waitlist.domain.WaitlistEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SlotOfferTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-10-10T10:00:00Z"),
            ZoneOffset.UTC
    );

    private AppointmentSlot slot;

    private WaitlistEntry entry;

    private SlotOffer offer;

    @BeforeEach
    void setUp() {

        slot = mock(AppointmentSlot.class);

        entry = mock(WaitlistEntry.class);

        offer = new SlotOffer(
                slot,
                entry,
                FIXED_CLOCK
        );
    }

    @Test
    void shouldCreatePendingOfferWithFifteenMinutesExpiration() {

        assertNotNull(offer.getId());

        assertNotNull(offer.getToken());

        assertEquals(
                SlotOfferStatus.PENDING,
                offer.getStatus()
        );

        assertEquals(
                Instant.parse("2026-10-10T10:00:00Z"),
                offer.getOfferedAt().toInstant()
        );

        assertEquals(
                Instant.parse("2026-10-10T10:15:00Z"),
                offer.getExpiresAt().toInstant()
        );

        assertNull(offer.getRespondedAt());

        assertSame(slot, offer.getAppointmentSlot());

        assertSame(entry, offer.getWaitlistEntry());
    }

    @Test
    void shouldAcceptOfferBeforeExpiration() {

        Clock responseClock = clockAt(
                "2026-10-10T10:14:00Z"
        );

        offer.accept(responseClock);

        assertEquals(
                SlotOfferStatus.ACCEPTED,
                offer.getStatus()
        );

        assertEquals(
                Instant.parse("2026-10-10T10:14:00Z"),
                offer.getRespondedAt().toInstant()
        );
    }

    @Test
    void shouldRejectOfferBeforeExpiration() {

        Clock responseClock = clockAt(
                "2026-10-10T10:05:00Z"
        );

        offer.reject(responseClock);

        assertEquals(
                SlotOfferStatus.REJECTED,
                offer.getStatus()
        );

        assertEquals(
                Instant.parse("2026-10-10T10:05:00Z"),
                offer.getRespondedAt().toInstant()
        );
    }

    @Test
    void shouldNotAcceptOfferAtExpirationTime() {

        Clock expiredClock = clockAt(
                "2026-10-10T10:15:00Z"
        );

        assertThrows(
                BusinessException.class,
                () -> offer.accept(expiredClock)
        );

        assertEquals(
                SlotOfferStatus.PENDING,
                offer.getStatus()
        );

        assertNull(offer.getRespondedAt());
    }

    @Test
    void shouldNotRejectExpiredOffer() {

        Clock expiredClock = clockAt(
                "2026-10-10T10:16:00Z"
        );

        assertThrows(
                BusinessException.class,
                () -> offer.reject(expiredClock)
        );

        assertEquals(
                SlotOfferStatus.PENDING,
                offer.getStatus()
        );

        assertNull(offer.getRespondedAt());
    }

    @Test
    void shouldExpireOfferAfterDeadline() {

        Clock expiredClock = clockAt(
                "2026-10-10T10:16:00Z"
        );

        offer.expire(expiredClock);

        assertEquals(
                SlotOfferStatus.EXPIRED,
                offer.getStatus()
        );

        assertNull(offer.getRespondedAt());
    }

    @Test
    void shouldNotExpireOfferBeforeDeadline() {

        Clock beforeExpiration = clockAt(
                "2026-10-10T10:14:00Z"
        );

        assertThrows(
                BusinessException.class,
                () -> offer.expire(beforeExpiration)
        );

        assertEquals(
                SlotOfferStatus.PENDING,
                offer.getStatus()
        );
    }

    @Test
    void shouldNotAcceptRejectedOffer() {

        offer.reject(
                clockAt("2026-10-10T10:05:00Z")
        );

        assertThrows(
                BusinessException.class,
                () -> offer.accept(
                        clockAt("2026-10-10T10:06:00Z")
                )
        );

        assertEquals(
                SlotOfferStatus.REJECTED,
                offer.getStatus()
        );
    }

    @Test
    void shouldNotRejectAcceptedOffer() {

        offer.accept(
                clockAt("2026-10-10T10:05:00Z")
        );

        assertThrows(
                BusinessException.class,
                () -> offer.reject(
                        clockAt("2026-10-10T10:06:00Z")
                )
        );

        assertEquals(
                SlotOfferStatus.ACCEPTED,
                offer.getStatus()
        );
    }

    @Test
    void shouldNotAcceptExpiredOffer() {

        offer.expire(
                clockAt("2026-10-10T10:16:00Z")
        );

        assertThrows(
                BusinessException.class,
                () -> offer.accept(
                        clockAt("2026-10-10T10:17:00Z")
                )
        );

        assertEquals(
                SlotOfferStatus.EXPIRED,
                offer.getStatus()
        );
    }

    @Test
    void shouldNotExpireAcceptedOffer() {

        offer.accept(
                clockAt("2026-10-10T10:05:00Z")
        );

        assertThrows(
                BusinessException.class,
                () -> offer.expire(
                        clockAt("2026-10-10T10:16:00Z")
                )
        );

        assertEquals(
                SlotOfferStatus.ACCEPTED,
                offer.getStatus()
        );
    }

    @Test
    void shouldNotCreateOfferWithoutSlot() {

        assertThrows(
                NullPointerException.class,
                () -> new SlotOffer(
                        null,
                        entry,
                        FIXED_CLOCK
                )
        );
    }

    @Test
    void shouldNotCreateOfferWithoutWaitlistEntry() {

        assertThrows(
                NullPointerException.class,
                () -> new SlotOffer(
                        slot,
                        null,
                        FIXED_CLOCK
                )
        );
    }

    private Clock clockAt(String instant) {

        return Clock.fixed(
                Instant.parse(instant),
                ZoneOffset.UTC
        );
    }
}