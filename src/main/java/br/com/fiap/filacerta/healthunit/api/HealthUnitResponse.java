package br.com.fiap.filacerta.healthunit.api;

import br.com.fiap.filacerta.healthunit.domain.HealthUnit;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HealthUnitResponse(
        UUID id,
        String code,
        String name,
        OffsetDateTime createdAt
) {

    public static HealthUnitResponse from(HealthUnit healthUnit) {
        return new HealthUnitResponse(
                healthUnit.getId(),
                healthUnit.getCode(),
                healthUnit.getName(),
                healthUnit.getCreatedAt()
        );
    }
}