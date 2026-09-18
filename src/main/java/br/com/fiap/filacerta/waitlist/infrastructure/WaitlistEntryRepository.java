package br.com.fiap.filacerta.waitlist.infrastructure;

import br.com.fiap.filacerta.waitlist.domain.WaitlistEntry;
import br.com.fiap.filacerta.waitlist.domain.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, UUID> {

    boolean existsByPatientIdAndHealthUnitIdAndSpecialtyIdAndStatus(
            UUID patientId,
            UUID healthUnitId,
            UUID specialtyId,
            WaitlistStatus status
    );
}
