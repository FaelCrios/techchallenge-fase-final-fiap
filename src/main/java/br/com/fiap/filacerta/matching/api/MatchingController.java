package br.com.fiap.filacerta.matching.api;

import br.com.fiap.filacerta.matching.application.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/slots")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/{slotId}/candidates")
    public ResponseEntity<List<MatchingCandidateResponse>> rankCandidates(@PathVariable UUID slotId) {
        List<MatchingCandidateResponse> candidates = matchingService.rankCandidates(slotId);
        return ResponseEntity.ok(candidates);
    }
}