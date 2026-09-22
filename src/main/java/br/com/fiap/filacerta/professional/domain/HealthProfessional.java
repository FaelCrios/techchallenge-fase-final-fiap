package br.com.fiap.filacerta.professional.domain;

import br.com.fiap.filacerta.healthunit.domain.HealthUnit;
import br.com.fiap.filacerta.specialty.domain.Specialty;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "health_professional")
public class HealthProfessional {

    @Id
    private UUID id;

    @Column(
            name = "name",
            nullable = false,
            length = 150
    )
    private String name;

    @Column(
            name = "professional_registration",
            nullable = false,
            unique = true,
            length = 50
    )
    private String professionalRegistration;

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

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected HealthProfessional() {
    }

    public HealthProfessional(
            String name,
            String professionalRegistration,
            HealthUnit healthUnit,
            Specialty specialty
    ) {
        this.id = UUID.randomUUID();

        this.name = Objects.requireNonNull(
                name, "O nome é obrigatório"
        ).trim();

        this.professionalRegistration =
                Objects.requireNonNull(
                        professionalRegistration,
                        "O registro profissional é obrigatório"
                ).trim().toUpperCase(java.util.Locale.ROOT);

        this.healthUnit = Objects.requireNonNull(
                healthUnit, "A unidade é obrigatória"
        );

        this.specialty = Objects.requireNonNull(
                specialty, "A especialidade é obrigatória"
        );

        if (this.name.isBlank()
                || this.professionalRegistration.isBlank()) {
            throw new IllegalArgumentException(
                    "Nome e registro profissional são obrigatórios"
            );
        }

        this.createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProfessionalRegistration() {
        return professionalRegistration;
    }

    public HealthUnit getHealthUnit() {
        return healthUnit;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}