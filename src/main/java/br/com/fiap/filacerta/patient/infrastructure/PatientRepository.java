package br.com.fiap.filacerta.patient.infrastructure;

import br.com.fiap.filacerta.patient.domain.Patient;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    boolean existsBySusIdentifier(String susIdentifier);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT patient
            FROM Patient patient
            WHERE patient.id = :id
            """)
    Optional<Patient> findByIdForUpdate(
            @Param("id") UUID id
    );
}