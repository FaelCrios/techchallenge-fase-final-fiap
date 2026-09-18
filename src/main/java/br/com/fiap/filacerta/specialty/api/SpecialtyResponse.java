package br.com.fiap.filacerta.specialty.api;

import br.com.fiap.filacerta.specialty.domain.Specialty;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SpecialtyResponse(
        UUID id,
        String code,
        String name,
        OffsetDateTime createdAt
) {
    public static SpecialtyResponse from(Specialty specialty) {
        return new SpecialtyResponse(specialty.getId(), specialty.getCode(), specialty.getName(), specialty.getCreatedAt());
    }
}
