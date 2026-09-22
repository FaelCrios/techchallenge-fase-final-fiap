package br.com.fiap.filacerta.offer.infrastructure;

import br.com.fiap.filacerta.offer.domain.SlotOffer;
import br.com.fiap.filacerta.offer.domain.SlotOfferStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SlotOfferRepository
        extends JpaRepository<SlotOffer, UUID> {

    Optional<SlotOffer> findByToken(UUID token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT offer
            FROM SlotOffer offer
            WHERE offer.token = :token
            """)
    Optional<SlotOffer> findByTokenForUpdate(@Param("token") UUID token);

    boolean existsByAppointmentSlotIdAndWaitlistEntryIdAndStatus(
            UUID appointmentSlotId,
            UUID waitlistEntryId,
            SlotOfferStatus status
    );

    boolean existsByAppointmentSlotIdAndWaitlistEntryIdAndStatusIn(
            UUID appointmentSlotId,
            UUID waitlistEntryId,
            List<SlotOfferStatus> statuses
    );

    @Query("""
            SELECT offer.id
            FROM SlotOffer offer
            WHERE offer.status = :status
              AND offer.expiresAt <= :now
            ORDER BY offer.expiresAt ASC
            """)
    List<UUID> findExpiredOfferIds(
            @Param("status") SlotOfferStatus status,
            @Param("now") OffsetDateTime now
    );

    @Query("""
            SELECT offer.appointmentSlot.id
            FROM SlotOffer offer
            WHERE offer.id = :offerId
            """)
    Optional<UUID> findSlotIdByOfferId(@Param("offerId") UUID offerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT offer
            FROM SlotOffer offer
            WHERE offer.id = :offerId
            """)
    Optional<SlotOffer> findByIdForUpdate(@Param("offerId") UUID offerId);

    @Query("""
        SELECT offer.appointmentSlot.id
        FROM SlotOffer offer
        WHERE offer.token = :token
        """)
    Optional<UUID> findSlotIdByToken(@Param("token") UUID token);
}