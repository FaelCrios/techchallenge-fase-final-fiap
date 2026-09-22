package br.com.fiap.filacerta.professional.api;

import br.com.fiap.filacerta.professional.application.HealthProfessionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/professionals")
public class HealthProfessionalController {

    private final HealthProfessionalService professionalService;

    public HealthProfessionalController(
            HealthProfessionalService professionalService
    ) {
        this.professionalService = professionalService;
    }

    @PostMapping
    public ResponseEntity<HealthProfessionalResponse> create(
            @Valid @RequestBody CreateHealthProfessionalRequest request
    ) {

        HealthProfessionalResponse response =
                professionalService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HealthProfessionalResponse> findById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                professionalService.findById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<HealthProfessionalResponse>> findAll() {

        return ResponseEntity.ok(
                professionalService.findAll()
        );
    }
}