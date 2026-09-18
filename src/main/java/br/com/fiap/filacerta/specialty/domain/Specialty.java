package br.com.fiap.filacerta.specialty.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name="specialty")
public class Specialty {

    @Id
    private UUID id;

    @Column(name="code",nullable = false,unique = true,length = 50)
    private String code;

    @Column(name="name",nullable = false,length = 120)
    private String name;

    @Column(name="created_at",nullable = false,updatable = false)
    private OffsetDateTime createdAt;

    protected  Specialty() {}

    public Specialty(String code, String name) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.name = name;
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
