package br.com.fiap.filacerta.patient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "patient")
public class Patient {

    @Id
    private UUID id;

    @Column(name="sus_identifier", nullable = false,unique = true,length = 30)
    private String susIdentifier;

    @Column(name = "name", nullable = false,length = 150)
    private String name;

    @Column(name="created_at", nullable = false,updatable = false)
    private OffsetDateTime createdAt;

    protected Patient() {}

    public Patient(String susIdentifier, String name) {
        this.id = UUID.randomUUID();
        this.susIdentifier = susIdentifier;
        this.name = name;
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public UUID getId() {
        return id;
    }

    public String getSusIdentifier() {
        return susIdentifier;
    }

    public String getName() {
        return name;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
