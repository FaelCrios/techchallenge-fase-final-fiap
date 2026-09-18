package br.com.fiap.filacerta.waitlist.domain;

import br.com.fiap.filacerta.healthunit.domain.HealthUnit;
import br.com.fiap.filacerta.patient.domain.Patient;
import br.com.fiap.filacerta.specialty.domain.Specialty;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "waitlist_entry")
public class WaitlistEntry {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "patient_id",
            nullable = false
    )
    private Patient patient;

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

    @Enumerated(EnumType.STRING)
    @Column(
            name = "clinical_priority",
            nullable = false,
            length = 30
    )
    private ClinicalPriority clinicalPriority;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "preferred_period",
            nullable = false,
            length = 30
    )
    private PreferredPeriod preferredPeriod;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private WaitlistStatus status;

    @Column(
            name = "entered_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime enteredAt;

    protected WaitlistEntry() {
    }

    public WaitlistEntry(
            Patient patient,
            HealthUnit healthUnit,
            Specialty specialty,
            ClinicalPriority clinicalPriority,
            PreferredPeriod preferredPeriod
    ) {
        this.id = UUID.randomUUID();
        this.patient = patient;
        this.healthUnit = healthUnit;
        this.specialty = specialty;
        this.clinicalPriority = clinicalPriority;
        this.preferredPeriod = preferredPeriod;
        this.status = WaitlistStatus.WAITING;
        this.enteredAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public UUID getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public HealthUnit getHealthUnit() {
        return healthUnit;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public ClinicalPriority getClinicalPriority() {
        return clinicalPriority;
    }

    public PreferredPeriod getPreferredPeriod() {
        return preferredPeriod;
    }

    public WaitlistStatus getStatus() {
        return status;
    }

    public OffsetDateTime getEnteredAt() {
        return enteredAt;
    }
}