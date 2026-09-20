package br.com.fiap.filacerta.scheduling.api;


import br.com.fiap.filacerta.scheduling.application.AppointmentSlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/slots")
public class AppointmentSlotController {

    private final AppointmentSlotService appointmentSlotService;

    public AppointmentSlotController(AppointmentSlotService appointmentSlotService) {
        this.appointmentSlotService = appointmentSlotService;
    }

    @PostMapping
    public ResponseEntity<AppointmentSlotResponse> create(@Valid @RequestBody CreateAppointmentSlotRequest request){
        AppointmentSlotResponse response = appointmentSlotService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentSlotResponse> findById(@PathVariable UUID id){
        return ResponseEntity.ok(appointmentSlotService.findById(id));
    }

    public ResponseEntity<List<AppointmentSlotResponse>> findAll(){
        return ResponseEntity.ok(appointmentSlotService.findAll());
    }
}
