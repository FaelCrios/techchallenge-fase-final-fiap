package br.com.fiap.filacerta.status.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/v1/status")
public class StatusController {

    @Operation(summary = "Verifica se a aplicação está disponível")
    @ApiResponse(responseCode = "200", description = "Aplicação disponível")
    @GetMapping
    public ResponseEntity<StatusResponse> getStatus() {
        final StatusResponse response = new StatusResponse(
                "FilaCerta SUS",
                "UP",
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        return ResponseEntity.ok(response);
    }
}
