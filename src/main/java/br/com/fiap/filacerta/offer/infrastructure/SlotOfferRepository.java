package br.com.fiap.filacerta.offer.infrastructure;


import br.com.fiap.filacerta.offer.domain.SlotOffer;
import br.com.fiap.filacerta.offer.domain.SlotOfferStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SlotOfferRepository extends JpaRepository<SlotOffer, UUID> {

    Optional<SlotOffer> findByToken(UUID token);

    boolean existsByAppointmentSlotIdAndWaitlistEntryIdAndStatus(UUID appointmentSlotId, UUID waitlistEntryId, SlotOfferStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT offer
        FROM SlotOffer offer
        WHERE offer.token = :token
        """)
    Optional<SlotOffer> findByTokenForUpdate(@Param("token") UUID token);
}
