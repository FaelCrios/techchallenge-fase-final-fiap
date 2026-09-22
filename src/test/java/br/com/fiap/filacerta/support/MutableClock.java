package br.com.fiap.filacerta.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

public final class MutableClock extends Clock {

    private volatile Instant instant;

    private final ZoneId zone;

    public MutableClock(
            Instant instant,
            ZoneId zone
    ) {
        this.instant = Objects.requireNonNull(
                instant,
                "O instante é obrigatório"
        );

        this.zone = Objects.requireNonNull(
                zone,
                "O fuso horário é obrigatório"
        );
    }

    public void setInstant(Instant instant) {
        this.instant = Objects.requireNonNull(
                instant,
                "O instante é obrigatório"
        );
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}