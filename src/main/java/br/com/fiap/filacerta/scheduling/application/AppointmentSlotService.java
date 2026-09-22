package br.com.fiap.filacerta.scheduling.application;

import br.com.fiap.filacerta.healthunit.domain.HealthUnit;
import br.com.fiap.filacerta.healthunit.infrastructure.HealthUnitRepository;
import br.com.fiap.filacerta.professional.domain.HealthProfessional;
import br.com.fiap.filacerta.professional.infrastructure.HealthProfessionalRepository;
import br.com.fiap.filacerta.scheduling.api.AppointmentSlotResponse;
import br.com.fiap.filacerta.scheduling.api.CreateAppointmentSlotRequest;
import br.com.fiap.filacerta.scheduling.domain.AppointmentSlot;
import br.com.fiap.filacerta.scheduling.infrastructure.AppointmentSlotRepository;
import br.com.fiap.filacerta.shared.exception.BusinessException;
import br.com.fiap.filacerta.shared.exception.ConflictException;
import br.com.fiap.filacerta.shared.exception.NotFoundException;
import br.com.fiap.filacerta.specialty.domain.Specialty;
import br.com.fiap.filacerta.specialty.infrastructure.SpecialtyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentSlotService {

    private final AppointmentSlotRepository appointmentSlotRepository;
    private final HealthUnitRepository healthUnitRepository;
    private final SpecialtyRepository specialtyRepository;
    private final HealthProfessionalRepository professionalRepository;

    public AppointmentSlotService(
            AppointmentSlotRepository appointmentSlotRepository,
            HealthUnitRepository healthUnitRepository,
            SpecialtyRepository specialtyRepository,
            HealthProfessionalRepository professionalRepository
    ) {
        this.appointmentSlotRepository = appointmentSlotRepository;
        this.healthUnitRepository = healthUnitRepository;
        this.specialtyRepository = specialtyRepository;
        this.professionalRepository = professionalRepository;
    }

    @Transactional
    public AppointmentSlotResponse create(
            CreateAppointmentSlotRequest request
    ) {

        HealthProfessional professional =
                professionalRepository
                        .findByIdForUpdate(request.professionalId())
                        .orElseThrow(() -> new NotFoundException(
                                "Profissional não encontrado: "
                                        + request.professionalId()
                        ));

        HealthUnit healthUnit = findHealthUnit(request.healthUnitId());

        Specialty specialty = findSpecialty(request.specialtyId());

        validateScheduledAt(request.scheduledAt());
        validateProfessional(
                professional,
                healthUnit,
                specialty
        );

        validateSlotAvailability(
                professional.getId(),
                request.scheduledAt()
        );
        AppointmentSlot slot = new AppointmentSlot(
                healthUnit,
                specialty,
                professional,
                request.scheduledAt()
        );

        AppointmentSlot savedSlot =
                appointmentSlotRepository.save(slot);

        return AppointmentSlotResponse.from(savedSlot);
    }

    @Transactional(readOnly = true)
    public AppointmentSlotResponse findById(UUID id) {
        AppointmentSlot slot =
                appointmentSlotRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException(
                                "Vaga não encontrada: " + id
                        ));

        return AppointmentSlotResponse.from(slot);
    }

    @Transactional(readOnly = true)
    public List<AppointmentSlotResponse> findAll() {
        return appointmentSlotRepository.findAll()
                .stream()
                .map(AppointmentSlotResponse::from)
                .toList();
    }

    private HealthUnit findHealthUnit(UUID healthUnitId) {
        return healthUnitRepository.findById(healthUnitId)
                .orElseThrow(() -> new NotFoundException(
                        "Unidade de saúde não encontrada: "
                                + healthUnitId
                ));
    }

    private Specialty findSpecialty(UUID specialtyId) {
        return specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new NotFoundException(
                        "Especialidade não encontrada: "
                                + specialtyId
                ));
    }

    private void validateScheduledAt(
            OffsetDateTime scheduledAt
    ) {
        if (!scheduledAt.isAfter(OffsetDateTime.now())) {
            throw new BusinessException("A data e horário da vaga devem estar no futuro");
        }
    }

    private void validateProfessional(
            HealthProfessional professional,
            HealthUnit healthUnit,
            Specialty specialty
    ) {
        if (!professional.getHealthUnit().getId()
                .equals(healthUnit.getId())) {

            throw new BusinessException("O profissional não pertence à unidade informada");
        }
        if (!professional.getSpecialty().getId()
                .equals(specialty.getId())) {

            throw new BusinessException("O profissional não atende a especialidade informada");
        }
    }

    private void validateSlotAvailability(
            UUID professionalId,
            OffsetDateTime scheduledAt
    ) {

        boolean alreadyExists =
                appointmentSlotRepository
                        .existsByProfessionalIdAndScheduledAt(
                                professionalId,
                                scheduledAt
                        );

        if (alreadyExists) {

            throw new ConflictException(
                    "O profissional já possui uma vaga cadastrada "
                            + "para este horário"
            );
        }
    }
}