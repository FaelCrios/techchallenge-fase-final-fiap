package br.com.fiap.filacerta.professional.application;

import br.com.fiap.filacerta.healthunit.domain.HealthUnit;
import br.com.fiap.filacerta.healthunit.infrastructure.HealthUnitRepository;
import br.com.fiap.filacerta.professional.api.CreateHealthProfessionalRequest;
import br.com.fiap.filacerta.professional.api.HealthProfessionalResponse;
import br.com.fiap.filacerta.professional.domain.HealthProfessional;
import br.com.fiap.filacerta.professional.infrastructure.HealthProfessionalRepository;
import br.com.fiap.filacerta.shared.exception.ConflictException;
import br.com.fiap.filacerta.shared.exception.NotFoundException;
import br.com.fiap.filacerta.specialty.domain.Specialty;
import br.com.fiap.filacerta.specialty.infrastructure.SpecialtyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class HealthProfessionalService {

    private final HealthProfessionalRepository professionalRepository;
    private final HealthUnitRepository healthUnitRepository;
    private final SpecialtyRepository specialtyRepository;

    public HealthProfessionalService(
            HealthProfessionalRepository professionalRepository,
            HealthUnitRepository healthUnitRepository,
            SpecialtyRepository specialtyRepository
    ) {
        this.professionalRepository = professionalRepository;
        this.healthUnitRepository = healthUnitRepository;
        this.specialtyRepository = specialtyRepository;
    }

    @Transactional
    public HealthProfessionalResponse create(
            CreateHealthProfessionalRequest request
    ) {

        String registration = request
                .professionalRegistration()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (professionalRepository
                .existsByProfessionalRegistration(registration)) {

            throw new ConflictException("Já existe um profissional com este registro");
        }

        HealthUnit healthUnit = healthUnitRepository
                .findById(request.healthUnitId())
                .orElseThrow(() -> new NotFoundException(
                        "Unidade de saúde não encontrada: "
                                + request.healthUnitId()
                ));

        Specialty specialty = specialtyRepository
                .findById(request.specialtyId())
                .orElseThrow(() -> new NotFoundException(
                        "Especialidade não encontrada: "
                                + request.specialtyId()
                ));

        HealthProfessional professional =
                new HealthProfessional(
                        request.name(),
                        registration,
                        healthUnit,
                        specialty
                );

        HealthProfessional saved =
                professionalRepository.save(professional);

        return HealthProfessionalResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public HealthProfessionalResponse findById(UUID id) {

        HealthProfessional professional =
                professionalRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException(
                                "Profissional não encontrado: " + id
                        ));

        return HealthProfessionalResponse.from(professional);
    }

    @Transactional(readOnly = true)
    public List<HealthProfessionalResponse> findAll() {

        return professionalRepository.findAll()
                .stream()
                .map(HealthProfessionalResponse::from)
                .toList();
    }
}