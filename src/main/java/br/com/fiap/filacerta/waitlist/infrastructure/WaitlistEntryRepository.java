package br.com.fiap.filacerta.waitlist.infrastructure;

import br.com.fiap.filacerta.waitlist.domain.WaitlistEntry;
import br.com.fiap.filacerta.waitlist.domain.WaitlistStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, UUID> {

    boolean existsByPatientIdAndHealthUnitIdAndSpecialtyIdAndStatus(
            UUID patientId,
            UUID healthUnitId,
            UUID specialtyId,
            WaitlistStatus status
    );

    List<WaitlistEntry> findByHealthUnitIdAndSpecialtyIdAndStatus(
            UUID healthUnitId,
            UUID specialtyId,
            WaitlistStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT entry
            FROM WaitlistEntry entry
            WHERE entry.id = :id
            """)
    Optional<WaitlistEntry> findByIdForUpdate(@Param("id") UUID id);

    boolean existsByPatientIdAndHealthUnitIdAndSpecialtyIdAndStatusIn(
            UUID patientId,
            UUID healthUnitId,
            UUID specialtyId,
            Collection<WaitlistStatus> statuses
    );
}
