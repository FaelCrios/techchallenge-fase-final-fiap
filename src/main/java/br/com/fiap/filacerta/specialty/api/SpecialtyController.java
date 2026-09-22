package br.com.fiap.filacerta.specialty.api;

import br.com.fiap.filacerta.specialty.application.SpecialtyService;
import br.com.fiap.filacerta.specialty.domain.Specialty;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/specialties")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @PostMapping
    public ResponseEntity<SpecialtyResponse> create(@Valid @RequestBody CreateSpecialtyRequest request) {
        SpecialtyResponse response = specialtyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialtyResponse> findByid(@PathVariable UUID id) {
        SpecialtyResponse response = specialtyService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SpecialtyResponse>> findAll() {
        List<SpecialtyResponse> response = specialtyService.findAll();
        return ResponseEntity.ok(response);
    }
}
