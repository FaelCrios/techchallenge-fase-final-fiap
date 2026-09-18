package br.com.fiap.filacerta.status.api;

import java.time.OffsetDateTime;

public record StatusResponse(
        String application,
        String status,
        OffsetDateTime timestamp
) {
}
