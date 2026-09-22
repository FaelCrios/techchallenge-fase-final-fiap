package br.com.fiap.filacerta.professional.infrastructure;

import br.com.fiap.filacerta.professional.domain.HealthProfessional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface HealthProfessionalRepository extends JpaRepository<HealthProfessional, UUID> {

    boolean existsByProfessionalRegistration(
            String professionalRegistration
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT professional
            FROM HealthProfessional professional
            WHERE professional.id = :id
            """)
    Optional<HealthProfessional> findByIdForUpdate(@Param("id") UUID id);
}