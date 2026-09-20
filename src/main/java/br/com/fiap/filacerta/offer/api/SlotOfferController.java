package br.com.fiap.filacerta.offer.api;

import br.com.fiap.filacerta.offer.application.SlotOfferService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/slots")
public class SlotOfferController {

    private final SlotOfferService slotOfferService;

    public SlotOfferController(
            SlotOfferService slotOfferService
    ) {
        this.slotOfferService = slotOfferService;
    }

    @PostMapping("/{slotId}/offers")
    public ResponseEntity<SlotOfferResponse> createOffer(@PathVariable UUID slotId) {
        SlotOfferResponse response =
                slotOfferService.createOffer(slotId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/offers/{token}")
    public ResponseEntity<SlotOfferResponse> findByToken(@PathVariable UUID token) {
        return ResponseEntity.ok(slotOfferService.findByToken(token));
    }

    @PostMapping("/offers/{token}/accept")
    public ResponseEntity<SlotOfferResponse> acceptOffer(@PathVariable UUID token) {
        return ResponseEntity.ok(slotOfferService.acceptOffer(token));
    }

    @PostMapping("/offers/{token}/reject")
    public ResponseEntity<SlotOfferResponse> rejectOffer(@PathVariable UUID token) {
        return ResponseEntity.ok(slotOfferService.rejectOffer(token));
    }
}