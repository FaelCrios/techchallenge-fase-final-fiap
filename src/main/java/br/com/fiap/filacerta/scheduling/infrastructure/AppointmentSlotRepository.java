package br.com.fiap.filacerta.scheduling.infrastructure;

import br.com.fiap.filacerta.scheduling.domain.AppointmentSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT slot
            FROM AppointmentSlot slot
            WHERE slot.id = :id
            """)
    Optional<AppointmentSlot> findByIdForUpdate(@Param("id") UUID id);
}
