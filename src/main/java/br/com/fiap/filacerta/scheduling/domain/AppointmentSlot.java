package br.com.fiap.filacerta.scheduling.domain;

import br.com.fiap.filacerta.healthunit.domain.HealthUnit;
import br.com.fiap.filacerta.professional.domain.HealthProfessional;
import br.com.fiap.filacerta.shared.exception.BusinessException;
import br.com.fiap.filacerta.specialty.domain.Specialty;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "appointment_slot")
public class AppointmentSlot {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "health_unit_id",
            nullable = false
    )
    private HealthUnit healthUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "specialty_id",
            nullable = false
    )
    private Specialty specialty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "professional_id"
    )
    private HealthProfessional professional;

    @Column(
            name = "scheduled_at",
            nullable = false
    )
    private OffsetDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private AppointmentSlotStatus status;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected AppointmentSlot() {
    }

    public AppointmentSlot(
            HealthUnit healthUnit,
            Specialty specialty,
            HealthProfessional professional,
            OffsetDateTime scheduledAt
    ) {

        this.healthUnit = Objects.requireNonNull(healthUnit, "A unidade é obrigatória");

        this.specialty = Objects.requireNonNull(specialty, "A especialidade é obrigatória");

        this.professional = Objects.requireNonNull(professional, "O profissional é obrigatório");

        this.scheduledAt = Objects.requireNonNull(scheduledAt, "O horário é obrigatório");

        if (!professional.getHealthUnit().getId()
                .equals(healthUnit.getId())) {

            throw new BusinessException("O profissional não pertence à unidade informada");
        }

        if (!professional.getSpecialty().getId()
                .equals(specialty.getId())) {

            throw new BusinessException("O profissional não atende a especialidade informada");
        }

        this.id = UUID.randomUUID();
        this.status = AppointmentSlotStatus.AVAILABLE;
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void markAsOffered() {

        if (this.status != AppointmentSlotStatus.AVAILABLE) {
            throw new BusinessException("A vaga não está disponível para oferta");
        }

        this.status = AppointmentSlotStatus.OFFERED;
    }

    public void markAsBooked() {
        if (this.status != AppointmentSlotStatus.OFFERED) {
            throw new BusinessException("A vaga não possui oferta pendente");
        }
        this.status = AppointmentSlotStatus.BOOKED;
    }

    public void release() {
        if (this.status != AppointmentSlotStatus.OFFERED) {
            throw new BusinessException("A vaga não está reservada para uma oferta");
        }
        this.status = AppointmentSlotStatus.AVAILABLE;
    }

    public UUID getId() {
        return id;
    }

    public HealthUnit getHealthUnit() {
        return healthUnit;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public HealthProfessional getProfessional() {
        return professional;
    }

    public OffsetDateTime getScheduledAt() {
        return scheduledAt;
    }

    public AppointmentSlotStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}