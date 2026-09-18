package br.com.fiap.filacerta.healthunit.api;

import br.com.fiap.filacerta.healthunit.application.HealthUnitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health-units")
public class HealthUnitController {

    private final HealthUnitService healthUnitService;

    public HealthUnitController(HealthUnitService healthUnitService) {
        this.healthUnitService = healthUnitService;
    }

    @PostMapping
    public ResponseEntity<HealthUnitResponse> create(
            @Valid
            @RequestBody
            CreateHealthUnitRequest createHealthUnitRequest
    ) {
        HealthUnitResponse healthUnitResponse = healthUnitService.create(createHealthUnitRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(healthUnitResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HealthUnitResponse> findById(@PathVariable UUID id) {
        HealthUnitResponse response = healthUnitService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<HealthUnitResponse>> findAll() {
        List<HealthUnitResponse> response = healthUnitService.findAll();
        return ResponseEntity.ok(response);
    }
}
