package br.com.fiap.filacerta.waitlist.application;

import br.com.fiap.filacerta.healthunit.domain.HealthUnit;
import br.com.fiap.filacerta.healthunit.infrastructure.HealthUnitRepository;
import br.com.fiap.filacerta.patient.domain.Patient;
import br.com.fiap.filacerta.patient.infrastructure.PatientRepository;
import br.com.fiap.filacerta.shared.exception.ConflictException;
import br.com.fiap.filacerta.shared.exception.NotFoundException;
import br.com.fiap.filacerta.specialty.domain.Specialty;
import br.com.fiap.filacerta.specialty.infrastructure.SpecialtyRepository;
import br.com.fiap.filacerta.waitlist.api.CreateWaitlistEntryRequest;
import br.com.fiap.filacerta.waitlist.api.WaitlistEntryResponse;
import br.com.fiap.filacerta.waitlist.domain.WaitlistEntry;
import br.com.fiap.filacerta.waitlist.domain.WaitlistStatus;
import br.com.fiap.filacerta.waitlist.infrastructure.WaitlistEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class WaitlistService {

    private final WaitlistEntryRepository waitlistEntryRepository;
    private final PatientRepository patientRepository;
    private final HealthUnitRepository healthUnitRepository;
    private final SpecialtyRepository specialtyRepository;

    public WaitlistService(
            WaitlistEntryRepository waitlistEntryRepository,
            PatientRepository patientRepository,
            HealthUnitRepository healthUnitRepository,
            SpecialtyRepository specialtyRepository
    ) {
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.patientRepository = patientRepository;
        this.healthUnitRepository = healthUnitRepository;
        this.specialtyRepository = specialtyRepository;
    }

    @Transactional
    public WaitlistEntryResponse create(
            CreateWaitlistEntryRequest request
    ) {
        Patient patient = findPatient(request.patientId());

        HealthUnit healthUnit =
                findHealthUnit(request.healthUnitId());

        Specialty specialty =
                findSpecialty(request.specialtyId());

        validatePatientIsNotAlreadyWaiting(
                patient.getId(),
                healthUnit.getId(),
                specialty.getId()
        );

        WaitlistEntry entry = new WaitlistEntry(
                patient,
                healthUnit,
                specialty,
                request.clinicalPriority(),
                request.preferredPeriod()
        );

        WaitlistEntry savedEntry =
                waitlistEntryRepository.save(entry);

        return WaitlistEntryResponse.from(savedEntry);
    }

    @Transactional(readOnly = true)
    public WaitlistEntryResponse findById(UUID id) {
        WaitlistEntry entry = waitlistEntryRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Entrada da fila não encontrada: " + id
                ));

        return WaitlistEntryResponse.from(entry);
    }

    @Transactional(readOnly = true)
    public List<WaitlistEntryResponse> findAll() {
        return waitlistEntryRepository.findAll()
                .stream()
                .map(WaitlistEntryResponse::from)
                .toList();
    }

    private Patient findPatient(UUID patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException(
                        "Paciente não encontrado: " + patientId
                ));
    }

    private HealthUnit findHealthUnit(UUID healthUnitId) {
        return healthUnitRepository.findById(healthUnitId)
                .orElseThrow(() -> new NotFoundException(
                        "Unidade de saúde não encontrada: " + healthUnitId
                ));
    }

    private Specialty findSpecialty(UUID specialtyId) {
        return specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new NotFoundException(
                        "Especialidade não encontrada: " + specialtyId
                ));
    }

    private void validatePatientIsNotAlreadyWaiting(
            UUID patientId,
            UUID healthUnitId,
            UUID specialtyId
    ) {
        boolean alreadyWaiting =
                waitlistEntryRepository
                        .existsByPatientIdAndHealthUnitIdAndSpecialtyIdAndStatus(
                                patientId,
                                healthUnitId,
                                specialtyId,
                                WaitlistStatus.WAITING
                        );

        if (alreadyWaiting) {
            throw new ConflictException(
                    "O paciente já está aguardando nesta fila"
            );
        }
    }

}
