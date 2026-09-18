package br.com.fiap.filacerta.patient.application;

import br.com.fiap.filacerta.patient.api.CreatePatientRequest;
import br.com.fiap.filacerta.patient.api.PatientResponse;
import br.com.fiap.filacerta.patient.domain.Patient;
import br.com.fiap.filacerta.patient.infrastructure.PatientRepository;
import br.com.fiap.filacerta.shared.exception.ConflictException;
import br.com.fiap.filacerta.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public PatientResponse create(CreatePatientRequest request) {
        String normalizedSusIdentifier =
                normalizeSusIdentifier(request.susIdentifier());

        String normalizedName = request.name().trim();

        validateSusIdentifierDoesNotExist(normalizedSusIdentifier);

        Patient patient = new Patient(
                normalizedSusIdentifier,
                normalizedName
        );

        Patient savedPatient = patientRepository.save(patient);

        return PatientResponse.from(savedPatient);
    }

    @Transactional(readOnly = true)
    public PatientResponse findById(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Paciente não encontrado: " + id
                ));

        return PatientResponse.from(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> findAll() {
        return patientRepository.findAll()
                .stream()
                .map(PatientResponse::from)
                .toList();
    }

    private void validateSusIdentifierDoesNotExist(
            String susIdentifier
    ) {
        if (patientRepository.existsBySusIdentifier(susIdentifier)) {
            throw new ConflictException(
                    "Já existe um paciente com o identificador SUS: "
                            + susIdentifier
            );
        }
    }

    private String normalizeSusIdentifier(String susIdentifier) {
        return susIdentifier.trim();
    }
}