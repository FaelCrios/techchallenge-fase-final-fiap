package br.com.fiap.filacerta.waitlist.api;

import br.com.fiap.filacerta.waitlist.application.WaitlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/waitlist")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(
            WaitlistService waitlistService
    ) {
        this.waitlistService = waitlistService;
    }

    @PostMapping
    public ResponseEntity<WaitlistEntryResponse> create(
            @Valid
            @RequestBody
            CreateWaitlistEntryRequest request
    ) {
        WaitlistEntryResponse response =
                waitlistService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WaitlistEntryResponse> findById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                waitlistService.findById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<WaitlistEntryResponse>> findAll() {
        return ResponseEntity.ok(
                waitlistService.findAll()
        );
    }
}